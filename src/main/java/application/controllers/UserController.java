package application.controllers;

import application.JaccardUserHelper;
import application.models.Product;
import application.models.Review;
import application.models.User;
import application.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

@Controller
public class UserController {

    @Autowired
    private UserRepository repository;

    public UserController(UserRepository repository) {
        this.repository = repository;
    }


    @GetMapping("/userProfile")
    public String userProfile() {
        return "userProfile";
    }

    @GetMapping("/followList")
    public String followList(HttpServletRequest request, Model model) throws ServletException, IOException {
        HttpSession session = request.getSession();
        long curUserID = (long) session.getAttribute("userId");
        User curUser = this.repository.findUserById(curUserID);

        Set<User> followers = curUser.getFollowersList();
        if (!followers.isEmpty())
            model.addAttribute("followers", followers);
        return "followList";
    }

    @GetMapping("/followingList")
    public String followingList(HttpServletRequest request, Model model) throws ServletException, IOException {
        HttpSession session = request.getSession();
        long curUserID = (long) session.getAttribute("userId");
        User curUser = this.repository.findUserById(curUserID);

        Set<User> following = curUser.getFollowingList();
        if (!following.isEmpty())
            model.addAttribute("following", following);
        return "followingList";
    }

    @GetMapping("/reviewsWritten")
    public String reviewsWritten(HttpServletRequest request, Model model) throws ServletException, IOException {
        HttpSession session = request.getSession();
        long curUserID = (long) session.getAttribute("userId");
        User curUser = this.repository.findUserById(curUserID);
        Set<Review> reviewsWritten = curUser.getReviewList();
        model.addAttribute("reviews", reviewsWritten);
        return "reviewsWritten";
    }

    @GetMapping("/viewUsers")
    public String viewUsers(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        long curUserID = (long) session.getAttribute("userId");
        User curUser = this.repository.findUserById(curUserID);

        Set<User> users = this.repository.findAll();
        users.remove(curUser);

        model.addAttribute("userslist", users);
        model.addAttribute("currUser", curUser);

        return "viewUsers";
    }

    @GetMapping("/followUser/{id}")
    public String followUser(@PathVariable long id, HttpServletRequest request,Model model) {
        User followUser = this.repository.findUserById(id);
        HttpSession session = request.getSession();
        long curUserID = (long) session.getAttribute("userId");
        User curUser = this.repository.findUserById(curUserID);

        curUser.followUser(followUser);
        this.repository.save(curUser);
        this.repository.save(followUser);

        model.addAttribute("followedUser", followUser);

        return "followUserSucess";
    }

    @GetMapping("/mostFollowedUsers")
    public String mostFolowedUsers(HttpServletRequest request, Model model) throws ServletException, IOException {
        HttpSession session = request.getSession();
        long curUserID = (long) session.getAttribute("userId");
        User curUser = this.repository.findUserById(curUserID);

        List<User> users = this.repository.findAllByIdIsNotNull();
        users.remove(curUser);
        List<Integer> sortedFollowers = new ArrayList<>();

        HashMap<String, Integer> resultsMap = new HashMap<>();

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            Set<User> followerList = user.getFollowersList();
            sortedFollowers.add(followerList.size());
            Collections.sort(sortedFollowers);

            for (int j = 0; j < sortedFollowers.size(); j++) {
                if (user.getFollowersList().size() == sortedFollowers.get(j)) {
                    resultsMap.put(user.getUsername(), sortedFollowers.get(j));
                }
            }
        }

        model.addAttribute("resultsMap",resultsMap);

        return "mostFollowedUsers";
    }

    @GetMapping("/similarUsers")
    public String similarUsers(HttpServletRequest request, Model model) throws ServletException, IOException {
        HttpSession session = request.getSession();
        long curUserID = (long) session.getAttribute("userId");
        User curUser = this.repository.findUserById(curUserID);

        Set<User> users = this.repository.findAll();
        users.remove(curUser);
        ArrayList<JaccardUserHelper> similarUsers = curUser.sortByJaccardDistance(users);
        model.addAttribute("users", similarUsers);
        return "similarUsers";
    }

}
