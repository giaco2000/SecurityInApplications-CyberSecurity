# Security In Applications-CyberSecurity

## 📋 Project Description

This repository contains the **Practical Exam** for the **Application Security course** at the University of Bari Aldo Moro, Department of Computer Science.

The project implements a **secure Java web application** demonstrating the application of cybersecurity principles and defensive programming in a real web environment.

## 🎯 Project Goals

The web application implements a complete system for managing **project proposals** with the following features:

### Main Features
- **🔐 Secure registration** with profile image (file format validation)
- **🔑 Authentication system** with Cookie management options
- **📄 Proposal upload** in .txt format with secure validation
- **👀 Proposal viewing** from other users
- **🚪 Secure logout** with session and cookie invalidation

### Implemented Security Aspects

#### 🛡️ Security
- **Secure password management** (hashing with salt)
- **SQL Injection prevention** (prepared statements)
- **XSS protection** (sanity checking, escaping)
- **File validation** with Apache Tika
- **Secure Cookie and HTTP Session management**
- **Cryptography** for sensitive data

#### 🔒 Defensive Programming
- **Variable scope minimization**
- **Class accessibility reduction** (information hiding)
- **TOCTOU prevention** (Time-Of-Check Time-Of-Use)
- **Thread-safety** with JCIP library
- **Structured feedback** for all methods

## 🧪 Testing

The project includes comprehensive documented testing:

### Usage Tests
- Registration (success/failure with invalid files)
- Login/Logout (with/without cookies)
- Proposal upload and viewing
- Session timeout management

### Abuse Tests
- Authentication bypass attempts (SQL Injection)
- Malicious file uploads (.exe, fake .jpeg)
- XSS attacks via project proposals
- General security testing

## 🛠️ Technologies Used

- **Java** (JSP, Servlets)
- **MySQL** (database)
- **Apache Tika** (file validation)
- **jsoup** (secure HTML parsing)
- **JCIP** (thread-safety)

## 📁 Repository Structure

```
├── WebApplication_GiacomoPagliara/    # Application source code
│   ├── WEB-INF/                       # Web configurations
│   ├── *.jsp                          # JSP pages
│   └── lib/                           # JAR dependencies
├── Documentazione_GiacomoPagliara/    # Complete documentation
└── README.md                          # This file
```

## 🎓 Academic Context

**Course:** Security in Application   
**University:** University of Bari Aldo Moro  
**Department:** Computer Science  

