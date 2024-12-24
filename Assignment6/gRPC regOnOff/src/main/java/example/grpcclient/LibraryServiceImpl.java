package example.grpcclient;

import io.grpc.stub.StreamObserver;
import service.*;

import java.util.ArrayList;
import java.util.List;

public class LibraryServiceImpl extends LibraryServiceGrpc.LibraryServiceImplBase {
    private final List<Book> books = new ArrayList<>();

    @Override
    public void addBook(AddBookRequest request, StreamObserver<AddBookResponse> responseObserver) {
        // Validate input
        if (request.getTitle().trim().isEmpty() || request.getAuthor().trim().isEmpty() || request.getYear() <= 0) {
            AddBookResponse response = AddBookResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Invalid book details")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        // Check for duplicate book
        for (Book book : books) {
            if (book.getTitle().equals(request.getTitle()) && book.getAuthor().equals(request.getAuthor())) {
                AddBookResponse response = AddBookResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Book already exists")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }
        }

        // Add the book
        Book book = Book.newBuilder()
                .setTitle(request.getTitle())
                .setAuthor(request.getAuthor())
                .setYear(request.getYear())
                .build();
        books.add(book);

        AddBookResponse response = AddBookResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Book added successfully")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void listBooks(ListBooksRequest request, StreamObserver<ListBooksResponse> responseObserver) {
        ListBooksResponse response = ListBooksResponse.newBuilder()
                .addAllBooks(books)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}