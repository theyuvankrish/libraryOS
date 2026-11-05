package com.library.controller;

import com.library.model.Book;
import com.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    // LIST + SEARCH
    @GetMapping
    public String listBooks(@RequestParam(required = false) String search, Model model) {
        model.addAttribute("firebaseConnected", bookService.isLiveFirebaseConnected());
        model.addAttribute("firebaseProjectId", bookService.getFirebaseProjectId());
        try {
            List<Book> books;
            if (search != null && !search.isBlank()) {
                books = bookService.searchBooks(search);
                model.addAttribute("search", search);
            } else {
                books = bookService.getAllBooks();
            }
            model.addAttribute("books", books);
            model.addAttribute("totalBooks", books.size());
            long available = books.stream().filter(b -> "AVAILABLE".equals(b.getStatus())).count();
            long outOfStock = books.stream().filter(b -> "OUT_OF_STOCK".equals(b.getStatus())).count();
            model.addAttribute("availableCount", available);
            model.addAttribute("outOfStockCount", outOfStock);
        } catch (Exception e) {
            model.addAttribute("error", "Firestore database not created yet in Firebase Console: " + e.getMessage());
            model.addAttribute("books", List.of());
            model.addAttribute("totalBooks", 0);
            model.addAttribute("availableCount", 0);
            model.addAttribute("outOfStockCount", 0);
        }
        return "books/list";
    }

    // REDIRECT ROOT
    @GetMapping("/")
    public String root() {
        return "redirect:/books";
    }

    // SHOW ADD FORM
    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("formTitle", "Add New Book");
        model.addAttribute("isEdit", false);
        return "books/form";
    }

    // SUBMIT ADD
    @PostMapping
    public String addBook(@Valid @ModelAttribute Book book,
                          BindingResult result,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Add New Book");
            model.addAttribute("isEdit", false);
            return "books/form";
        }
        try {
            String id = bookService.addBook(book);
            redirectAttributes.addFlashAttribute("successMessage",
                "\u2713 Book \"" + book.getTitle() + "\" added successfully! [ID: " + id + "]");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add book: " + e.getMessage());
        }
        return "redirect:/books";
    }

    // DETAIL VIEW
    @GetMapping("/{id}")
    public String viewBook(@PathVariable String id, Model model) {
        try {
            Book book = bookService.getBookById(id);
            if (book == null) {
                model.addAttribute("error", "Book not found with ID: " + id);
                return "error";
            }
            model.addAttribute("book", book);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
        return "books/detail";
    }

    // SHOW EDIT FORM
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable String id, Model model) {
        try {
            Book book = bookService.getBookById(id);
            if (book == null) return "redirect:/books";
            model.addAttribute("book", book);
            model.addAttribute("formTitle", "Edit Book");
            model.addAttribute("isEdit", true);
        } catch (Exception e) {
            return "redirect:/books";
        }
        return "books/form";
    }

    // SUBMIT EDIT
    @PostMapping("/{id}/update")
    public String updateBook(@PathVariable String id,
                             @Valid @ModelAttribute Book book,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Edit Book");
            model.addAttribute("isEdit", true);
            book.setId(id);
            return "books/form";
        }
        try {
            bookService.updateBook(id, book);
            redirectAttributes.addFlashAttribute("successMessage",
                "\u2713 Book \"" + book.getTitle() + "\" updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update: " + e.getMessage());
        }
        return "redirect:/books";
    }

    // DELETE
    @PostMapping("/{id}/delete")
    public String deleteBook(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            Book book = bookService.getBookById(id);
            String title = book != null ? book.getTitle() : id;
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("successMessage",
                "\u2713 Book \"" + title + "\" deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete: " + e.getMessage());
        }
        return "redirect:/books";
    }
}
