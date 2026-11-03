<p align="center">
  <img src="frontend/src/assets/images/logos/magno.svg" width="300" alt="Magno Logo">
</p>

# Magno - Research Seedbeds Management System

![Project Status](https://img.shields.io/badge/Status-Development-orange)
![Spring Boot](https://img.shields.io/badge/Backend-Spring%20Boot-brightgreen)
![Vue 3](https://img.shields.io/badge/Frontend-Vue%203-blue)
![Vuetify](https://img.shields.io/badge/UI-Vuetify%203-lightblue)

**Magno** is a specialized administrative dashboard designed for higher education institutions to manage research seedbeds (*Semilleros de Investigación*). It streamlines the orchestration of investigation groups, student participation, and administrative compliance.

---

## 🚀 Key Features

- **🎓 Academic Management:** Orchestrate academic periods, departments, and programs.
- **🌱 Research Seedbeds:** Create and manage research seedbeds and investigation groups.
- **👥 User & Role Management:** Granular role-based access control (DIRI, Coordinators, Tutors, Students).
- **📝 Certificate Automation:** One-click generation of PDF certificates for students participating in seedbeds.
- **📊 Reporting Suite:** Export detailed Excel/PDF reports on investigation metrics and active groups.
- **🔐 Secure Authentication:** Seamless integration with Google OAuth2 for institutional login.
- **📋 Audit Logging:** Comprehensive monitoring of system activities and cronjob executions.

---

## 🛠️ Technology Stack

### Backend
- **Core:** Java 21+ with **Spring Boot 3.x**
- **Persistence:** Spring Data JPA + PostgreSQL
- **Security:** Spring Security & Google OAuth2
- **Build Tool:** Gradle
- **Documentation:** MapStruct for DTO mapping

### Frontend
- **Framework:** Vue 3 (Composition API)
- **UI Library:** Vuetify 3 (Material Design)
- **State Management:** Pinia
- **Build Tool:** Vite
- **Language:** TypeScript
- **Styling:** SASS / Iconify (Remix Icon)

---

## 📂 Project Structure

```bash
Magno/
├── backend/            # Spring Boot Application
│   ├── src/main/java/  # Hexagonal/Clean Architecture layers
│   └── build.gradle    # Backend dependencies
├── frontend/           # Vue 3 Single Page Application
│   ├── src/components/ # Reusable UI components
│   └── src/stores/     # Pinia state definitions
└── HU/                 # User Stories & Project Documentation
```

---

## 🏁 Getting Started

### Prerequisites
- JDK 17 or higher
- Node.js (v26.13.0)
- PostgreSQL Database

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/JackBlaze132/Magno.git
   ```

2. **Backend Setup:**
   Navigate to `backend/`, configure your `application.properties` (or environment variables) and run:
   ```bash
   ./gradlew bootRun
   ```

3. **Frontend Setup:**
   Navigate to `frontend/`, install dependencies and start the dev server:
   ```bash
   pnpm install
   pnpm dev
   ```

---


*Developed for educational excellence and research management.*
Eder M. | Santiago T. | Esteban G.

