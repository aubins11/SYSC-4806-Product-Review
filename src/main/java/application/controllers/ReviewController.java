package application.controllers;

import application.models.Product;
import application.models.Review;
import application.models.User;
import application.repositories.ProductRepository;
import application.repositories.ReviewRepository;
import application.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;


@Controller
public class ReviewController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    public ReviewController(UserRepository userRepository, ProductRepository productRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
    }
    @GetMapping("/createreview/{id}")
    public String createProductReviewForm(@PathVariable long id, Model model) {
        model.addAttribute("id", id);
        Review r = new Review();
        r.setProduct(productRepository.findById(id));
        model.addAttribute("review", r);
        return "createreview";
    }

    @PostMapping("/createreview/{id}")
    public String createProductReviewSuccess(@PathVariable long id, @ModelAttribute Review review, Model model, HttpServletRequest request) throws ServletException, IOException{
        Product currProduct = this.productRepository.findById(id);
        currProduct.addReview(review);
        HttpSession session = request.getSession();
        String username = (String)session.getAttribute("username");
        User currUser = this.userRepository.findByUsername(username);
        currUser.writeReview(currProduct, review.getReviewRating(), review.getReviewComment());
        review.setReviewAuthor(currUser);
        model.addAttribute("id", id);
        this.productRepository.save(currProduct);
        this.userRepository.save(currUser);
        return "reviewSuccess";
    }


    @GetMapping("/viewreviews/{id}")
    public String viewReviewsForProduct(@PathVariable long id, Model model) {
        Product currentProduct = this.productRepository.findById(id);
        if (currentProduct == null){
            return "invalidproduct";
        }
        model.addAttribute("currentProduct", currentProduct);
        model.addAttribute("productReviews", currentProduct.getReviews());
        model.addAttribute("id", currentProduct.getId());
        return "viewreviews";
    }

    @GetMapping("/filterreviews/{id}")
    public String filterReviewsByJaccard(@PathVariable long id, Model model, HttpServletRequest request) {
        Product currentProduct = this.productRepository.findById(id);
        if (currentProduct == null){
            return "invalidproduct";
        }
        model.addAttribute("currentProduct", currentProduct);
        HttpSession session = request.getSession();
        String username = (String)session.getAttribute("username");
        User currUser = this.userRepository.findByUsername(username);
        LinkedHashMap<Review, Double> jaccardOrdered = currentProduct.getReviewsSortedByJaccardDistance(currUser);
        ArrayList<Review> productReviews = new ArrayList<>();
        ArrayList<Double> indexes = new ArrayList<>();
        for (Map.Entry<Review, Double> entry : jaccardOrdered.entrySet()) {
            productReviews.add(entry.getKey());
            indexes.add(entry.getValue());
        }
        model.addAttribute("distances", indexes);
        model.addAttribute("sorted", "true");
        model.addAttribute("productReviews", productReviews);
        model.addAttribute("id", currentProduct.getId());
        return "viewreviews";
    }


}
