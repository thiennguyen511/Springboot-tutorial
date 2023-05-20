package com.tutorial.apidemo.Springboottutorial.controller;

import com.tutorial.apidemo.Springboottutorial.models.Product;
import com.tutorial.apidemo.Springboottutorial.models.ResponseObject;
import com.tutorial.apidemo.Springboottutorial.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "api/v1/Products")
public class ProductController {
    //DI = Dependency Injection
    @Autowired
    private ProductRepository repository;

    @GetMapping("")
    //this request is : http://localhost:8080/api/v1/Products
    ResponseEntity<ResponseObject>  getAllProducts() {
        // you must save this to Database, now we have H2 DB = in-memory Database
        List<Product> productList = repository.findAll(); // where is data ?
        return productList.size() > 0 ?
            ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject("ok","Query all product successfully", productList)
            ):
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject("failed","Query all product failed", productList));

    }

    @GetMapping("/{id}")
    // let's return an object with : data, message, status
    ResponseEntity<ResponseObject> findById(@PathVariable Long id) {
        Optional<Product> foundProduct = repository.findById(id);
        return foundProduct.isPresent() ?
            ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "Query product successfully", foundProduct)
            ):
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseObject("failed", "cannot find prouduct with id = " + id, "")
            );
    }

    // insert new product with POST method
    // Postman : Raw,Json
    @PostMapping("/insert")
    ResponseEntity<ResponseObject> insertProduct(@RequestBody Product newProduct) {
        // 2 products must not the same name
        return repository.findByProductName(newProduct.getProductName().trim()).size() > 0 ?
        ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(
                new ResponseObject("ok", "this product id is already exist", repository.findByProductName(newProduct.getProductName().trim()))
        ):
        ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject("ok", "insert new product successfully ", repository.save(newProduct))
        );
    }

    // update, upsert = update if found, otherwise insert
    @PutMapping("/{id}")
    ResponseEntity<ResponseObject> updateProduct(@RequestBody Product newProduct, @PathVariable Long id) {
        Product updateProduct = repository.findById(id)
                .map(product -> {
                    product.setProductName(newProduct.getProductName());
                    product.setProductYear(newProduct.getProductYear());
                    product.setUrl(newProduct.getUrl());
                    product.setPrice(newProduct.getPrice());
                    return repository.save(product);
                }).orElseGet(() -> {
                    newProduct.setId(id);
                    return repository.save(newProduct);
                });
        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject("ok", "update new product successfully ", updateProduct)
        );
    }

    // delete Product
    @DeleteMapping("/{id}")
    ResponseEntity<ResponseObject> deleteProduct(@PathVariable long id) {
        Boolean exist = repository.existsById(id);
        if(exist) {
            repository.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "delete product with id = " + id + " is successfully", "")
            );
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "cannot find product id is :" + id , "")
            );
        }
    }

}
