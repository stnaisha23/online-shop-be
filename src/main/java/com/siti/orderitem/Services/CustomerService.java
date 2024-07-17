package com.siti.orderitem.Services;

import com.siti.orderitem.Models.Customer;
import com.siti.orderitem.Repositories.CustomerRepository;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Date;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MinioClient minioClient;

    @Value("${application.minio.bucketName}")
    private String bucketName;

    public Page<Customer> getAllCustomers(int page, int size, String sortBy, String direction, String customerName) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Customer> allCustomersPage;
        if (customerName == null || customerName.isEmpty()) {
            allCustomersPage = customerRepository.findAllBy(pageable);
        } else {
            allCustomersPage = customerRepository.findAllByCustomerNameContainingIgnoreCase(customerName, pageable);
        }
        
        return allCustomersPage.map(this::setPicUrl);
    }

    // public Page<Customer> getAllCustomers(int page, int size) {
    //     Pageable pageable = PageRequest.of(page, size);
    //     return customerRepository.findAll(pageable).map(this::setPicUrl);
    // }
    

    public Page<Customer> getAllActiveCustomers(int page, int size, String sortBy, String direction, String customerName) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Customer> activeCustomersPage;
        if (customerName == null || customerName.isEmpty()) {
            activeCustomersPage = customerRepository.findByIsActive(true, pageable);
        } else {
            activeCustomersPage = customerRepository.findByCustomerNameContainingIgnoreCaseAndIsActive(customerName, true, pageable);
        }
        
        return activeCustomersPage.map(this::setPicUrl);
    }

    public Optional<Customer> getCustomerById(Long customerId) {
        Optional<Customer> customerOptional = customerRepository.findByCustomerId(customerId);
        return customerOptional.map(this::setPicUrl);
    }

    private Customer setPicUrl(Customer customer) {
        String pic = customer.getPic();
        if (pic != null) {
            String picUrl = "http://localhost:9000/" + bucketName + "/" + pic;
            customer.setPic(picUrl);
        }
        return customer;
    }
    
    public Customer addCustomer(Customer newCustomer, MultipartFile pic) throws Exception {
        long timestamp = new Date().getTime();
    
        String customerPhone = newCustomer.getCustomerPhone();
        newCustomer.setCustomerCode("CUST-" + customerPhone);
    
        if (newCustomer.getIsActive() == null) {
            newCustomer.setIsActive(true);
        }
    
        if (newCustomer.getLastOrderDate() == null) {
            newCustomer.setLastOrderDate(new Date());
        }
    
        if (customerRepository.existsByCustomerPhone(customerPhone)) {
            throw new Exception("Nomor telepon sudah tersedia");
        }
    
        if (pic != null) {
            String originalFilename = pic.getOriginalFilename();
            String[] fileNameParts = originalFilename.split("\\.");
            String fileName = fileNameParts[0] + "." + timestamp + "." + fileNameParts[1];
    
            try (InputStream inputStream = pic.getInputStream()) {
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(inputStream, pic.getSize(), -1)
                        .contentType(pic.getContentType())
                        .build()
                );
            }
    
            newCustomer.setPic(fileName);
        }
    
        // Save customer ke database
        Customer savedCustomer = customerRepository.save(newCustomer);
    
        // Update customerCode with "CUST-" + customerId
        savedCustomer.setCustomerCode("CUST-" + savedCustomer.getCustomerId());
        return customerRepository.save(savedCustomer);
    }
    
    public Customer updateCustomerById(Long customerId, Customer updatedCustomer, MultipartFile pic) throws Exception {
        Optional<Customer> customerOptional = customerRepository.findById(customerId);
        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();
    
            // Update hanya field yang tidak null
            if (updatedCustomer.getCustomerName() != null) {
                customer.setCustomerName(updatedCustomer.getCustomerName());
            }
            if(updatedCustomer.getCustomerAddress() != null) {
                customer.setCustomerAddress(updatedCustomer.getCustomerAddress());
            }
            if (updatedCustomer.getCustomerCode() != null) {
                customer.setCustomerCode(updatedCustomer.getCustomerCode());
            }
            if (updatedCustomer.getCustomerPhone() != null) {
                customer.setCustomerPhone(updatedCustomer.getCustomerPhone());
            }
            if (updatedCustomer.getIsActive() != null) {
                customer.setIsActive(updatedCustomer.getIsActive());
            }
            if (updatedCustomer.getLastOrderDate() != null) {
                customer.setLastOrderDate(updatedCustomer.getLastOrderDate());
            }
            
            // Hapus file lama di MinIO jika ada file baru yang diunggah
            if (pic != null && customer.getPic() != null) {
                try {
                    minioClient.removeObject(
                        RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(customer.getPic())
                            .build()
                    );
                } catch (Exception e) {
                    throw new RuntimeException("Error deleting old file from MinIO", e);
                }
            }
    
            // Jika ada file baru, unggah ke MinIO
            if (pic != null) {
                long timestamp = new Date().getTime();
                String originalFilename = pic.getOriginalFilename();
                String[] fileNameParts = originalFilename.split("\\.");
                String fileName = fileNameParts[0] + "." + timestamp + "." + fileNameParts[1];
    
                try (InputStream inputStream = pic.getInputStream()) {
                    minioClient.putObject(
                        PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, pic.getSize(), -1)
                            .contentType(pic.getContentType())
                            .build()
                    );
                } catch (IOException e) {
                    throw new RuntimeException("Error uploading new file to MinIO", e);
                }
    
                customer.setPic(fileName);
            }
    
            return customerRepository.save(customer);
        } else {
            return null;
        }
    }
    

    public void deleteCustomerId(Long customerId) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId);
        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();
            if (!customer.getIsActive()) {
                throw new IllegalArgumentException("Data dengan ID tersebut sudah dihapus.");
            }
    
            // Hapus file di MinIO jika ada
            if (customer.getPic() != null) {
                try {
                    minioClient.removeObject(
                        RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(customer.getPic())
                            .build()
                    );
                } catch (Exception e) {
                    throw new RuntimeException("Error deleting file from MinIO", e);
                }
            }
    
            // Hapus data dari database
            customerRepository.deleteById(customerId);
        } else {
            throw new IllegalArgumentException("ID tidak ditemukan.");
        }
    }
    
}
