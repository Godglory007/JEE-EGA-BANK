package ma.enset.digitalbanking_spring_angular.mappers;

import ma.enset.digitalbanking_spring_angular.dtos.*;
import ma.enset.digitalbanking_spring_angular.entities.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BankAccountMapperImpl {
    public CustomerDTO fromCustomer(Customer customer) {
        CustomerDTO customerDTO = new CustomerDTO();
        BeanUtils.copyProperties(customer, customerDTO);
        return customerDTO;
    }
    
    public Customer toCustomerDTO(CustomerDTO customerDTO) {
        Customer customer = new Customer();
        BeanUtils.copyProperties(customerDTO, customer);
        return customer;
    }
    
    public CustomerFullDTO toCustomerFullDTO(Customer customer, AppUser appUser) {
        CustomerFullDTO dto = new CustomerFullDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setDateOfBirth(customer.getDateOfBirth());
        
        if (appUser != null) {
            dto.setUsername(appUser.getUsername());
        }
        
        if (customer.getBankAccounts() != null) {
            List<AccountInfoDTO> accounts = customer.getBankAccounts().stream()
                    .map(this::toAccountInfoDTO)
                    .collect(Collectors.toList());
            dto.setAccounts(accounts);
        } else {
            dto.setAccounts(new ArrayList<>());
        }
        
        return dto;
    }
    
    public AccountInfoDTO toAccountInfoDTO(BankAccount account) {
        AccountInfoDTO dto = new AccountInfoDTO();
        dto.setAccountNumber(account.getId());
        dto.setBalance(account.getBalance());
        dto.setStatus(account.getStatus().name());
        dto.setCreationDate(account.getCreationDate());
        
        if (account instanceof CurrentAccount) {
            dto.setAccountType("CURRENT");
        } else if (account instanceof SavingAccount) {
            dto.setAccountType("SAVING");
        }
        
        return dto;
    }

    public SavingBankAccountDTO fromSavingBankAccount(SavingAccount savingBankAccount) {
        SavingBankAccountDTO savingBankAccountDTO = new SavingBankAccountDTO();
        BeanUtils.copyProperties(savingBankAccount,savingBankAccountDTO);
        savingBankAccountDTO.setCustomerDTO(fromCustomer(savingBankAccount.getCustomer()));
        savingBankAccountDTO.setType(savingBankAccount.getClass().getSimpleName());
        return savingBankAccountDTO;
    }
    public SavingAccount fromSavingBankAccountDTO(SavingBankAccountDTO savingBankAccountDTO) {
        SavingAccount savingAccount = new SavingAccount();
        BeanUtils.copyProperties(savingBankAccountDTO,savingAccount);
        savingAccount.setCustomer(toCustomerDTO(savingBankAccountDTO.getCustomerDTO()));
        return savingAccount;
    }

    public CurrentBankAccountDTO fromCurrentBankAccount(CurrentAccount currentBankAccount) {
        CurrentBankAccountDTO currentBankAccountDTO = new CurrentBankAccountDTO();
        BeanUtils.copyProperties(currentBankAccount,currentBankAccountDTO);
        currentBankAccountDTO.setCustomerDTO(fromCustomer(currentBankAccount.getCustomer()));
        currentBankAccountDTO.setType(currentBankAccount.getClass().getSimpleName());
        return currentBankAccountDTO;
    }

    public CurrentAccount fromCurrentBankAccountDTO(CurrentBankAccountDTO currentBankAccountDTO) {
        CurrentAccount currentAccount = new CurrentAccount();
        BeanUtils.copyProperties(currentBankAccountDTO,currentAccount);
        currentAccount.setCustomer(toCustomerDTO(currentBankAccountDTO.getCustomerDTO()));
        return currentAccount;
    }

    public AccountOperationDTO fromAccountOperation(AccountOperation accountOperation) {
        AccountOperationDTO accountOperationDTO = new AccountOperationDTO();
        BeanUtils.copyProperties(accountOperation,accountOperationDTO);
        return accountOperationDTO;
    }
}
