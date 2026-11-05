package com.library.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.library.config.FirebaseConfig;
import com.library.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    private static final String COLLECTION = "books";

    @Autowired(required = false)
    private Firestore firestore;

    // In-memory store fallback pre-seeded with sample books
    private final Map<String, Book> localStore = new ConcurrentHashMap<>();

    public BookService() {
        seedLocalStore();
    }

    private void seedLocalStore() {
        Book b1 = new Book("BK-1001", "Clean Code", "Robert C. Martin", "9780132350884", "Computer Science", "Prentice Hall", 12, "AVAILABLE");
        Book b2 = new Book("BK-1002", "Designing Data-Intensive Applications", "Martin Kleppmann", "9781449373320", "Technology & Engineering", "O'Reilly Media", 6, "AVAILABLE");
        Book b3 = new Book("BK-1003", "The Pragmatic Programmer", "Andrew Hunt", "9780135957059", "Computer Science", "Addison-Wesley", 2, "LOW_STOCK");
        Book b4 = new Book("BK-1004", "Introduction to Algorithms", "Thomas H. Cormen", "9780262033848", "Mathematics", "MIT Press", 0, "OUT_OF_STOCK");
        Book b5 = new Book("BK-1005", "Spring in Action", "Craig Walls", "9781617294945", "Technology & Engineering", "Manning", 8, "AVAILABLE");

        localStore.put(b1.getId(), b1);
        localStore.put(b2.getId(), b2);
        localStore.put(b3.getId(), b3);
        localStore.put(b4.getId(), b4);
        localStore.put(b5.getId(), b5);
    }

    public boolean isLiveFirebaseConnected() {
        return firestore != null;
    }

    public String getFirebaseProjectId() {
        return FirebaseConfig.PROJECT_ID;
    }

    // CREATE
    public String addBook(Book book) throws ExecutionException, InterruptedException {
        String bookId = "BK-" + System.currentTimeMillis();
        book.setId(bookId);
        book.setStatus(book.computeStatus());

        if (firestore != null) {
            Map<String, Object> docData = bookToMap(book);
            ApiFuture<WriteResult> future = firestore.collection(COLLECTION).document(bookId).set(docData);
            future.get();
        } else {
            localStore.put(bookId, book);
        }
        return bookId;
    }

    // READ ALL
    public List<Book> getAllBooks() throws ExecutionException, InterruptedException {
        if (firestore != null) {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION).get();
            List<QueryDocumentSnapshot> docs = future.get().getDocuments();
            return docs.stream().map(this::docToBook).collect(Collectors.toList());
        } else {
            return new ArrayList<>(localStore.values());
        }
    }

    // READ ONE
    public Book getBookById(String id) throws ExecutionException, InterruptedException {
        if (firestore != null) {
            ApiFuture<DocumentSnapshot> future = firestore.collection(COLLECTION).document(id).get();
            DocumentSnapshot doc = future.get();
            if (doc.exists()) {
                return docToBook(doc);
            }
            return null;
        } else {
            return localStore.get(id);
        }
    }

    // SEARCH
    public List<Book> searchBooks(String query) throws ExecutionException, InterruptedException {
        List<Book> all = getAllBooks();
        String q = query.toLowerCase();
        return all.stream()
                .filter(b -> (b.getTitle() != null && b.getTitle().toLowerCase().contains(q))
                        || (b.getAuthor() != null && b.getAuthor().toLowerCase().contains(q))
                        || (b.getCategory() != null && b.getCategory().toLowerCase().contains(q))
                        || (b.getIsbn() != null && b.getIsbn().contains(q))
                        || (b.getId() != null && b.getId().toLowerCase().contains(q)))
                .collect(Collectors.toList());
    }

    // UPDATE
    public void updateBook(String id, Book book) throws ExecutionException, InterruptedException {
        book.setId(id);
        book.setStatus(book.computeStatus());

        if (firestore != null) {
            Map<String, Object> docData = bookToMap(book);
            ApiFuture<WriteResult> future = firestore.collection(COLLECTION).document(id).set(docData);
            future.get();
        } else {
            localStore.put(id, book);
        }
    }

    // DELETE
    public void deleteBook(String id) throws ExecutionException, InterruptedException {
        if (firestore != null) {
            ApiFuture<WriteResult> future = firestore.collection(COLLECTION).document(id).delete();
            future.get();
        } else {
            localStore.remove(id);
        }
    }

    // ---- Helpers ----
    private Map<String, Object> bookToMap(Book book) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", book.getId());
        map.put("title", book.getTitle());
        map.put("author", book.getAuthor());
        map.put("isbn", book.getIsbn());
        map.put("category", book.getCategory());
        map.put("publisher", book.getPublisher());
        map.put("quantity", book.getQuantity());
        map.put("status", book.getStatus());
        return map;
    }

    private Book docToBook(DocumentSnapshot doc) {
        return Book.builder()
                .id(doc.getString("id"))
                .title(doc.getString("title"))
                .author(doc.getString("author"))
                .isbn(doc.getString("isbn"))
                .category(doc.getString("category"))
                .publisher(doc.getString("publisher"))
                .quantity(doc.getLong("quantity") != null ? doc.getLong("quantity").intValue() : 0)
                .status(doc.getString("status"))
                .build();
    }
}
