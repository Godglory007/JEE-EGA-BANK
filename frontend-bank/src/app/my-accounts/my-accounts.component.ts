import { Component, OnInit } from '@angular/core';
import { CustomerService } from '../services/customer.service';
import { AccountsService } from '../services/accounts.service';
import jsPDF from 'jspdf';

@Component({
  selector: 'app-my-accounts',
  templateUrl: './my-accounts.component.html',
  styleUrls: ['./my-accounts.component.css']
})
export class MyAccountsComponent implements OnInit {
  accounts: any[] = [];
  operations: any[] = [];
  selectedAccount: any = null;
  isLoading: boolean = true;
  errorMessage: string = '';

  // Pour les opérations
  operationType: string = 'CREDIT';
  amount: number = 0;
  description: string = '';
  destinationAccount: string = '';
  operationLoading: boolean = false;
  operationSuccess: string = '';
  operationError: string = '';

  // Logo en base64
  private logoBase64: string = '';

  // Fonction pour formater les nombres (évite les problèmes avec toLocaleString dans jsPDF)
  private formatNumber(num: number): string {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
  }

  constructor(
    private customerService: CustomerService,
    private accountsService: AccountsService
  ) { }

  ngOnInit(): void {
    this.loadMyAccounts();
    this.loadLogo();
  }

  loadLogo(): void {
    const img = new Image();
    img.crossOrigin = 'Anonymous';
    img.onload = () => {
      const canvas = document.createElement('canvas');
      canvas.width = img.width;
      canvas.height = img.height;
      const ctx = canvas.getContext('2d');
      ctx?.drawImage(img, 0, 0);
      this.logoBase64 = canvas.toDataURL('image/png');
    };
    img.src = '/assets/img/logo-egabank.png';
  }

  loadMyAccounts(): void {
    this.isLoading = true;
    this.customerService.getMyAccounts().subscribe({
      next: (data) => {
        this.accounts = data;
        this.isLoading = false;
        if (this.accounts.length > 0) {
          this.selectAccount(this.accounts[0]);
        }
      },
      error: (err) => {
        this.errorMessage = 'Erreur lors du chargement de vos comptes';
        this.isLoading = false;
      }
    });
  }

  selectAccount(account: any): void {
    this.selectedAccount = account;
    this.loadOperations(account.id);
  }

  loadOperations(accountId: string): void {
    this.accountsService.getAccountHistory(accountId, 0, 20).subscribe({
      next: (data) => {
        this.operations = data.accountOperations || [];
      },
      error: (err) => {
        console.error('Erreur lors du chargement des opérations', err);
      }
    });
  }

  executeOperation(): void {
    if (!this.selectedAccount || this.amount <= 0) {
      this.operationError = 'Veuillez sélectionner un compte et entrer un montant valide';
      return;
    }

    this.operationLoading = true;
    this.operationSuccess = '';
    this.operationError = '';

    const accountId = this.selectedAccount.id;

    if (this.operationType === 'CREDIT') {
      this.accountsService.credit(accountId, this.amount, this.description).subscribe({
        next: () => this.handleSuccess('Dépôt effectué avec succès'),
        error: (err) => this.handleError(err)
      });
    } else if (this.operationType === 'DEBIT') {
      this.accountsService.debit(accountId, this.amount, this.description).subscribe({
        next: () => this.handleSuccess('Retrait effectué avec succès'),
        error: (err) => this.handleError(err)
      });
    } else if (this.operationType === 'TRANSFER') {
      if (!this.destinationAccount) {
        this.operationError = 'Veuillez entrer le numéro du compte destinataire';
        this.operationLoading = false;
        return;
      }
      this.accountsService.transfer(accountId, this.destinationAccount, this.amount, this.description).subscribe({
        next: () => this.handleSuccess('Virement effectué avec succès'),
        error: (err) => this.handleError(err)
      });
    }
  }

  private handleSuccess(message: string): void {
    this.operationLoading = false;
    this.operationSuccess = message;
    this.amount = 0;
    this.description = '';
    this.destinationAccount = '';
    this.loadMyAccounts();
    if (this.selectedAccount) {
      this.loadOperations(this.selectedAccount.id);
    }
  }

  private handleError(err: any): void {
    this.operationLoading = false;
    this.operationError = err.error?.message || 'Erreur lors de l\'opération';
  }

  downloadStatement(): void {
    if (!this.selectedAccount || this.operations.length === 0) {
      return;
    }

    const account = this.selectedAccount;
    const now = new Date();
    const dateStr = now.toLocaleDateString('fr-FR');
    
    // Créer le PDF
    const doc = new jsPDF();
    const pageWidth = doc.internal.pageSize.getWidth();
    
    // Logo
    if (this.logoBase64) {
      doc.addImage(this.logoBase64, 'PNG', 15, 10, 40, 20);
    }
    
    doc.setFontSize(16);
    doc.setTextColor(0, 0, 0);
    doc.text('RELEVÉ DE COMPTE BANCAIRE', pageWidth / 2, 45, { align: 'center' });
    
    // Ligne de séparation
    doc.setDrawColor(0, 51, 102);
    doc.setLineWidth(0.5);
    doc.line(15, 50, pageWidth - 15, 50);
    
    // Date d'édition
    doc.setFontSize(10);
    doc.text(`Date d'édition: ${dateStr}`, pageWidth - 60, 58);
    
    // Informations du compte
    doc.setFontSize(12);
    doc.setTextColor(0, 51, 102);
    doc.text('INFORMATIONS DU COMPTE', 15, 70);
    
    doc.setFontSize(10);
    doc.setTextColor(0, 0, 0);
    const ownerName = account.customerDTO?.name || 'Non spécifié';
    doc.text(`Propriétaire: ${ownerName}`, 15, 80);
    doc.text(`Numéro de compte: ${account.id}`, 15, 87);
    doc.text(`Type de compte: ${account.type === 'CurrentAccount' ? 'Compte Courant' : 'Compte Épargne'}`, 15, 94);
    doc.text(`Statut: ${account.status}`, 15, 101);
    doc.setFont('helvetica', 'bold');
    doc.text(`Solde actuel: ${this.formatNumber(account.balance)} FCFA`, 15, 108);
    doc.setFont('helvetica', 'normal');
    
    // Tableau des opérations
    doc.setFontSize(12);
    doc.setTextColor(0, 51, 102);
    doc.text('HISTORIQUE DES OPÉRATIONS', 15, 122);
    
    // En-têtes du tableau
    let y = 132;
    doc.setFillColor(0, 51, 102);
    doc.rect(15, y - 5, pageWidth - 30, 8, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(9);
    doc.text('Date', 20, y);
    doc.text('Type', 55, y);
    doc.text('Description', 85, y);
    doc.text('Montant (FCFA)', 150, y);
    
    y += 10;
    doc.setTextColor(0, 0, 0);
    
    let totalCredit = 0;
    let totalDebit = 0;
    
    this.operations.forEach((op, index) => {
      if (y > 260) {
        doc.addPage();
        y = 20;
      }
      
      const date = new Date(op.operationDate).toLocaleDateString('fr-FR');
      const type = op.operationType === 'CREDIT' ? 'Dépôt' : 'Retrait';
      const desc = (op.description || '-').substring(0, 25);
      const sign = op.operationType === 'CREDIT' ? '+' : '-';
      const amount = `${sign}${this.formatNumber(op.amount)}`;
      
      if (op.operationType === 'CREDIT') {
        totalCredit += op.amount;
        doc.setTextColor(0, 128, 0);
      } else {
        totalDebit += op.amount;
        doc.setTextColor(255, 0, 0);
      }
      
      // Ligne alternée
      if (index % 2 === 0) {
        doc.setFillColor(240, 240, 240);
        doc.rect(15, y - 4, pageWidth - 30, 7, 'F');
      }
      
      doc.setTextColor(0, 0, 0);
      doc.text(date, 20, y);
      doc.text(type, 55, y);
      doc.text(desc, 85, y);
      
      if (op.operationType === 'CREDIT') {
        doc.setTextColor(0, 128, 0);
      } else {
        doc.setTextColor(255, 0, 0);
      }
      doc.text(amount, 150, y);
      
      y += 7;
    });
    
    // Résumé
    y += 10;
    doc.setDrawColor(0, 51, 102);
    doc.line(15, y, pageWidth - 15, y);
    y += 10;
    
    doc.setFontSize(11);
    doc.setTextColor(0, 51, 102);
    doc.text('RÉSUMÉ', 15, y);
    y += 10;
    
    doc.setFontSize(10);
    doc.setTextColor(0, 128, 0);
    doc.text(`Total des depots: +${this.formatNumber(totalCredit)} FCFA`, 15, y);
    y += 7;
    doc.setTextColor(255, 0, 0);
    doc.text(`Total des retraits: -${this.formatNumber(totalDebit)} FCFA`, 15, y);
    y += 7;
    doc.setTextColor(0, 0, 0);
    doc.setFont('helvetica', 'bold');
    doc.text(`Solde actuel: ${this.formatNumber(account.balance)} FCFA`, 15, y);
    
    // Pied de page
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(8);
    doc.setTextColor(128, 128, 128);
    doc.text('Merci pour votre confiance.', pageWidth / 2, 280, { align: 'center' });
    doc.text('Ce document est généré automatiquement par EGABANK.', pageWidth / 2, 285, { align: 'center' });
    
    // Télécharger le PDF
    doc.save(`releve_compte_${account.id}_${now.toISOString().split('T')[0]}.pdf`);
  }
}
