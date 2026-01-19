import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Customer} from "../model/customer.model";

export interface CreateCustomerRequest {
  name: string;
  email: string;
  phone?: string;
  dateOfBirth?: Date;
  password: string;
  accountType: 'CURRENT' | 'SAVING';
  initialBalance?: number;
  overdraft?: number;
  interestRate?: number;
}

export interface AccountInfo {
  accountNumber: string;
  accountType: string;
  balance: number;
  status: string;
  creationDate: Date;
}

export interface CustomerFull {
  id: number;
  name: string;
  email: string;
  phone?: string;
  dateOfBirth?: Date;
  username: string;
  accounts: AccountInfo[];
}

@Injectable({
  providedIn: 'root'
})
export class CustomerService {

  host: string = 'http://localhost:8080';

  constructor(private http: HttpClient) { }

  public getCustomers():Observable<Array<Customer>>{
    return this.http.get<Array<Customer>>(this.host+'/customers');
  }

  public getCustomer(id: number): Observable<Customer> {
    return this.http.get<Customer>(`${this.host}/customers/${id}`);
  }

  public searchCustomers(name: string):Observable<Array<Customer>>{
    return this.http.get<Array<Customer>>(this.host+`/customers/search?keyword=${name}`);
  }

  public saveCustomer(customer: Customer):Observable<Customer>{
    return this.http.post<Customer>(this.host+'/customers', customer);
  }

  public deleteCustomer(id: number){
    return this.http.delete(this.host+"/customers/"+id);
  }

  public updateCustomer(customer: Customer):Observable<Customer>{
    return this.http.put<Customer>(this.host+`/customers/${customer.id}`, customer);
  }

  // Nouvelles méthodes pour création complète de client
  public createCustomerFull(request: CreateCustomerRequest): Observable<CustomerFull> {
    return this.http.post<CustomerFull>(this.host + '/customers/full', request);
  }

  public getCustomerFull(id: number): Observable<CustomerFull> {
    return this.http.get<CustomerFull>(`${this.host}/customers/${id}/full`);
  }

  public getMyAccounts(): Observable<any[]> {
    return this.http.get<any[]>(this.host + '/my-accounts');
  }

  public getMyOperations(page: number = 0, size: number = 10): Observable<any[]> {
    return this.http.get<any[]>(`${this.host}/my-operations?page=${page}&size=${size}`);
  }
}
