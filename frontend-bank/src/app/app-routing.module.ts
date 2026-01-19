import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AccountsComponent} from "./accounts/accounts.component";
import {CustomersComponent} from "./customers/customers.component";
import {NewCustomerComponent} from "./new-customer/new-customer.component";
import {NewCustomerFullComponent} from "./new-customer-full/new-customer-full.component";
import {MyAccountsComponent} from "./my-accounts/my-accounts.component";
import {EditCustomerComponent} from "./edit-customer/edit-customer.component";
import {LoginComponent} from "./login/login.component";
import {AuthenticationGuard} from "./guards/authentication.guard";
import {AuthorizationGuard} from './guards/authorization.guard';
import {HomeComponent} from "./home/home.component";
import {DashboardComponent} from "./dashboard/dashboard.component";
import {ProfilComponent} from "./profil/profil.component";
import {NotAuthorizedComponent} from "./not-authorized/not-authorized.component";
import {TemplateComponent} from "./template/template.component";
import {CustomerAccountsComponent} from "./customer-accounts/customer-accounts.component";

const routes: Routes = [
  {path: "login", component: LoginComponent},
  {
    path: "admin",
    component: TemplateComponent,
    canActivate: [AuthenticationGuard, AuthorizationGuard],
    children: [
      {path: "accounts", component: AccountsComponent},
      {path: "account/:id", component: AccountsComponent},
      {path: "accountsList", component: CustomerAccountsComponent},
      {path: "profile", component: ProfilComponent},
      {path: "dashboard", component: DashboardComponent},
      {path: "customers", component: CustomersComponent},
      {path: "new-customer", component: NewCustomerComponent},
      {path: "new-customer-full", component: NewCustomerFullComponent},
      {path: "edit-customer/:id", component: EditCustomerComponent},
      {path: "home", component: HomeComponent},
    ]
  },
  {
    path: "user",
    component: TemplateComponent,
    canActivate: [AuthenticationGuard],
    children: [
      {path: "home", component: HomeComponent},
      {path: "profile", component: ProfilComponent},
      {path: "my-accounts", component: MyAccountsComponent},
      {path: 'not-authorized', component: NotAuthorizedComponent},
    ]
  },
  // Redirection par défaut vers la page de connexion
  {path: "", redirectTo: "/login", pathMatch: "full"},
  {path: "**", redirectTo: "/login"}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
