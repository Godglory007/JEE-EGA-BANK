import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CustomerService, CreateCustomerRequest } from '../services/customer.service';

@Component({
  selector: 'app-new-customer-full',
  templateUrl: './new-customer-full.component.html',
  styleUrls: ['./new-customer-full.component.css']
})
export class NewCustomerFullComponent implements OnInit {
  customerForm!: FormGroup;
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;
  createdCustomer: any = null;

  // Liste des codes pays
  countryCodes = [
    { code: '+228', country: 'Togo', flag: '🇹🇬' },
    { code: '+233', country: 'Ghana', flag: '🇬🇭' },
    { code: '+229', country: 'Bénin', flag: '🇧🇯' },
    { code: '+225', country: 'Côte d\'Ivoire', flag: '🇨🇮' },
    { code: '+226', country: 'Burkina Faso', flag: '🇧🇫' },
    { code: '+227', country: 'Niger', flag: '🇳🇪' },
    { code: '+234', country: 'Nigeria', flag: '🇳🇬' },
    { code: '+221', country: 'Sénégal', flag: '🇸🇳' },
    { code: '+223', country: 'Mali', flag: '🇲🇱' },
    { code: '+224', country: 'Guinée', flag: '🇬🇳' },
    { code: '+237', country: 'Cameroun', flag: '🇨🇲' },
    { code: '+241', country: 'Gabon', flag: '🇬🇦' },
    { code: '+242', country: 'Congo', flag: '🇨🇬' },
    { code: '+243', country: 'RD Congo', flag: '🇨🇩' },
    { code: '+33', country: 'France', flag: '🇫🇷' },
    { code: '+1', country: 'USA/Canada', flag: '🇺🇸' },
    { code: '+44', country: 'Royaume-Uni', flag: '🇬🇧' },
    { code: '+49', country: 'Allemagne', flag: '🇩🇪' },
    { code: '+32', country: 'Belgique', flag: '🇧🇪' },
    { code: '+41', country: 'Suisse', flag: '🇨🇭' },
    { code: '+212', country: 'Maroc', flag: '🇲🇦' },
    { code: '+216', country: 'Tunisie', flag: '🇹🇳' },
    { code: '+213', country: 'Algérie', flag: '🇩🇿' }
  ];

  constructor(
    private fb: FormBuilder,
    private customerService: CustomerService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.customerForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      countryCode: ['+228'],
      phone: [''],
      dateOfBirth: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(4)]],
      confirmPassword: ['', Validators.required],
      accountType: ['CURRENT', Validators.required],
      initialBalance: [0, [Validators.min(0)]],
      overdraft: [500],
      interestRate: [5.5]
    }, { validator: this.passwordMatchValidator });
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('password')?.value === g.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  onSubmit(): void {
    if (this.customerForm.invalid) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const formValue = this.customerForm.value;
    
    // Combiner le code pays et le numéro de téléphone
    const fullPhone = formValue.phone ? `${formValue.countryCode} ${formValue.phone}` : '';
    
    const request: CreateCustomerRequest = {
      name: formValue.name,
      email: formValue.email,
      phone: fullPhone,
      dateOfBirth: formValue.dateOfBirth,
      password: formValue.password,
      accountType: formValue.accountType,
      initialBalance: formValue.initialBalance
    };

    if (formValue.accountType === 'CURRENT') {
      request.overdraft = formValue.overdraft;
    } else {
      request.interestRate = formValue.interestRate;
    }

    this.customerService.createCustomerFull(request).subscribe({
      next: (customer) => {
        this.isLoading = false;
        this.createdCustomer = customer;
        this.successMessage = `Client "${customer.name}" créé avec succès!`;
        this.customerForm.reset({
          accountType: 'CURRENT',
          initialBalance: 0,
          overdraft: 500,
          interestRate: 5.5
        });
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Erreur lors de la création du client';
      }
    });
  }

  get accountType() {
    return this.customerForm.get('accountType')?.value;
  }
}
