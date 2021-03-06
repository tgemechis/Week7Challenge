package me.afua.week7challenge;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.sql.rowset.spi.XmlReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


@Controller
public class MainController {
    @Autowired
    NewsService theWire;

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    AppRoleRepository roleRepo;


    @RequestMapping("/")
    public String showIndex(Model model, Authentication auth)
    {

        AppUser thisUser = new AppUser();
        RestTemplate restTemplate = new RestTemplate();
        model.addAttribute("topstories",theWire.getStories("top-headlines","general"));

        if(auth!=null)
        {
            model.addAttribute("myNews",theWire.getUserNewsDisplay(auth));
            System.out.println(theWire.getUserNewsDisplay(auth).toString());
            if(thisUser.getTopics().size()>0)
                System.out.println("Topic stories:"+theWire.getUserTopicDisplay(auth));

            if(thisUser.getCategories().size()>0)
                System.out.println("User stories:"+theWire.getUserNewsDisplay(auth).toString());

            model.addAttribute("myTopics",theWire.getUserTopicDisplay(auth));

        }


        return "newindex";
    }

    @RequestMapping("/signup")
    public String signup(Model model)
    {
        model.addAttribute("thisUser",new AppUser());
        return "signup";
    }

    @RequestMapping("/myprofile")
    public String myProfile(Model model)
    {
        AppUser theUser = appUserRepository.findById(new Long (1)).get();
        model.addAttribute("thisUser",theUser);
        return "userprofile";
    }


    @PostMapping("/register")
    public String registerUser(@ModelAttribute("thisUser") AppUser thisUser, BindingResult result)
    {
        ArrayList<String> userCategories = new ArrayList<>();
        userCategories.add("business");
        userCategories.add("health");
        thisUser.setCategories(userCategories);
        appUserRepository.save(thisUser);
        thisUser.addRole(roleRepo.findAppRoleByRoleName("USER"));
        return "redirect:/";
    }


    @GetMapping("/addcategory")
    public String addCategory(Model model, Authentication auth)
    {
        System.out.println(auth.getName());
        model.addAttribute("user", appUserRepository.findByUsername(auth.getName()));
        return "addcategory";
    }

    @PostMapping("/addcategory")
    public String saveCategory(@ModelAttribute("user") AppUser thisUser, HttpServletRequest request, BindingResult result, Model model, Authentication auth)
    {
        System.out.println(auth.getName());
        String toAdd = request.getParameter("addcategory");
        thisUser.getCategories().add(toAdd);
        System.out.println(thisUser.getCategories());
        appUserRepository.save(thisUser);
        System.out.println(thisUser);

        return "redirect:/myprofile";
    }

    @GetMapping("/addtopic")
    public String addTopic(Model model, Authentication auth)
    {
        System.out.println(auth.getName());
        model.addAttribute("user", appUserRepository.findByUsername(auth.getName()));
        return "addtopics";
    }

    @PostMapping("/addtopic")
    public String saveTopic(@ModelAttribute("user") AppUser thisUser, HttpServletRequest request, BindingResult result, Model model, Authentication auth)
    {

        System.out.println(auth.getName());
        String toAdd = request.getParameter("addtopic");
        thisUser.getTopics().add(toAdd);
        System.out.println(thisUser.getTopics());
        appUserRepository.save(thisUser);
        System.out.println(thisUser);

        return "redirect:/myprofile";
    }

    @GetMapping("/delete/category/{category}")
    public String deleteCategory(@PathVariable("category") String category, Authentication auth)
    {
        AppUser thisUser = appUserRepository.findByUsername(auth.getName());
        thisUser.getCategories().remove(category);
        appUserRepository.save(thisUser);
        return "redirect:/myprofile";
    }

    @GetMapping("/delete/topic/{topic}")
    public String deleteTopic(@PathVariable("topic") String topic, Authentication auth)
    {
        AppUser thisUser = appUserRepository.findByUsername(auth.getName());
        thisUser.getTopics().remove(topic);
        appUserRepository.save(thisUser);
        return "redirect:/myprofile";
    }


    @GetMapping("/testjoy")
    public @ResponseBody String testJoy()
    {

        //https://cdn.ghanaweb.com/feed/newsfeed.xml
        //return theWire.getGHNews("http://www.myjoyonline.com/pages/rss/site_edition.xml").toString();
        return theWire.getGHNews("https://cdn.ghanaweb.com/feed/newsfeed.xml").toString();
    }

}
