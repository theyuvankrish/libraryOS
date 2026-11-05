# 📚 LibraryOS — Library Book Management System

A full-stack CRUD Library Book Management web application built with **Spring Boot & Spring MVC**, backed by **Google Cloud Firebase (Firestore)**, and designed using a punchy **Neo-Brutalism design system** with 3D drop shadows and frosted glassmorphism navigation.

---

## 🌟 Key Features

- **Full CRUD Workflow**:
  - **Create**: Add new books with ID, Title, Author, ISBN, Category, Publisher, and Quantity.
  - **Read**: Live inventory list with computed status (`AVAILABLE`, `LOW_STOCK`, `OUT_OF_STOCK`), stock gauges, and dashboard counters.
  - **Search**: Live real-time query filtering across titles, authors, categories, ISBNs, and Book IDs.
  - **Update**: Edit book metadata and stock quantities.
  - **Delete**: Record removal with interactive Brutalist confirmation modals.
- **Backend & Persistence**:
  - Powered by **Spring Boot 3.3.5 (Java 17+)**.
  - Direct cloud integration with **Google Cloud Firestore** using the official **Firebase Admin SDK**.
- **Frontend & Design System**:
  - **Neo-Brutalism Design Philosophy**: Thick 3px black borders, vibrant electric palette (`#FFDE59`, `#4ECCA3`, `#FF4949`), and tactile 3D offset drop shadows (`4px 4px 0px #000`).
  - **Glassmorphism Header**: Modern frosted glass topbar (`backdrop-filter: blur(16px)`).
  - Responsive layouts rendered server-side with **Thymeleaf**.

---

## 🏗️ Architecture

```
Client (Browser)
   └── Thymeleaf HTML5 + Neo-Brutalism CSS (brutalist.css)
         └── Spring MVC Controller (@Controller - BookController)
               └── Service Layer (@Service - BookService)
                     └── Firebase Admin SDK (FirestoreClient)
                           └── Google Cloud Firestore (Database: miniproject-fd03e)
```

---

## 🚀 Getting Started

### 1. Prerequisites
- **Java 17 or higher** (Tested on Java 21/25 LTS)
- **Apache Maven 3.9+**
- A **Google Firebase Project** with Cloud Firestore enabled

### 2. Firebase Setup
1. Open the [Firebase Console](https://console.firebase.google.com/).
2. Create/Select your project and create a **Cloud Firestore database**.
3. Navigate to **Project Settings ⚙️ → Service accounts → Firebase Admin SDK**.
4. Click **Generate new private key** and download the `.json` key.
5. Place the file at:
   ```
   src/main/resources/serviceAccountKey.json
   ```

### 3. Run the Application
```bash
# Clone the repository
git clone https://github.com/theyuvankrish/libraryOS.git
cd libraryOS

# Run with Maven
mvn spring-boot:run
```

Open your browser at:
👉 **http://localhost:8080** (or `http://localhost:8080/books`)

---

## ☁️ Deploying to Vercel (or Cloud Containers)

This repository includes a multi-stage `Dockerfile` and `vercel.json` configured for Vercel's **Container Runtime**.

### Deploy Steps:
1. Import your GitHub repository `https://github.com/theyuvankrish/libraryOS` into [Vercel](https://vercel.com/new).
2. Vercel will detect `vercel.json` / `Dockerfile.vercel` automatically as a Container project.
3. In **Environment Variables**, add:
   - **`FIREBASE_SERVICE_ACCOUNT_JSON`**: Paste your full Firebase service account `.json` content here (or use `FIREBASE_KEY_BASE64`).
4. Click **Deploy**! Vercel will build the Maven image, attach its dynamic `$PORT`, and provide your live URL.

---

## 📂 Project Structure

```
library-management/
├── pom.xml                               # Dependencies (Spring Web, Thymeleaf, Firebase Admin SDK)
├── firebase.json                         # Firebase service & emulator configuration
├── .firebaserc                           # Firebase project configuration
├── dataconnect/                          # Firebase SQL Connect schemas & operations
│   ├── dataconnect.yaml
│   ├── schema/schema.gql
│   └── connector/
├── src/main/
│   ├── java/com/library/
│   │   ├── LibraryManagementApplication.java
│   │   ├── config/FirebaseConfig.java    # Firebase Admin SDK initialization bean
│   │   ├── controller/
│   │   │   ├── BookController.java       # CRUD endpoints & routing
│   │   │   └── RootController.java       # Root redirect
│   │   ├── model/Book.java               # Book entity with validation
│   │   └── service/BookService.java      # Firestore persistence layer
│   └── resources/
│       ├── application.properties
│       ├── static/
│       │   ├── css/brutalist.css         # Neo-Brutalism design system
│       │   └── images/books.svg          # Vector logo
│       └── templates/
│           ├── books/
│           │   ├── list.html             # Catalog, search & stats dashboard
│           │   ├── form.html             # Create / Edit book form
│           │   └── detail.html           # Book details & stock gauge
│           └── error.html                # Error page
```

---

## 📄 License
MIT License

