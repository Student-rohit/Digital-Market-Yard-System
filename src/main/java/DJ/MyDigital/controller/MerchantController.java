package DJ.MyDigital.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import DJ.MyDigital.Model.HMProduct;
import DJ.MyDigital.Model.MProduct;
import DJ.MyDigital.Model.Merchant;
import DJ.MyDigital.service.HMProductService;
import DJ.MyDigital.service.MProductService;
import DJ.MyDigital.service.MerchantService;
import DJ.MyDigital.webScraping.ScrapingService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/Merchant")
public class MerchantController {

    @Autowired
    private ScrapingService scrapingService;

    @Autowired
    private MProductService productService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private HMProductService HMproductService;

    @Autowired
    private JavaMailSender mailSender;

    private Map<String, String> otpMap = new HashMap<>();

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("merchant", new Merchant());
        return "MerchantRegistration";
    }

    @PostMapping("/register")
    public String registerMerchant(@ModelAttribute Merchant merchant, Model model, HttpServletRequest request) {
        String email = merchant.getMEmail();

        if (merchantService.emailExists(email)) {
            model.addAttribute("error", "Email already exists. Please use a different email.");
            return "MerchantRegistration";
        }

        // Generate and send OTP
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        otpMap.put(email, otp);

        try {
            sendOtpEmail(email, otp);
        } catch (Exception e) {
            model.addAttribute("error", "Error sending OTP. Please try again.");
            return "MerchantRegistration";
        }

        // Store merchant object in session (ensure session is created)
        HttpSession session = request.getSession(true);
        session.setAttribute("pendingMerchant", merchant);

        model.addAttribute("email", email);
        return "VerifyOtp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String otp, @RequestParam String email,
                            Model model, HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null) {
            model.addAttribute("error", "Session expired. Please register again.");
            model.addAttribute("email", email);
            return "MerchantRegistration";
        }

        String correctOtp = otpMap.get(email);
        Merchant merchant = (Merchant) session.getAttribute("pendingMerchant");

        if (correctOtp != null && correctOtp.equals(otp) && merchant != null && merchant.getMEmail().equals(email)) {
            merchantService.registerMerchant(merchant);
            String s = "Welcome to MyDigital! \nWe are excited to have you with us."
                    + "MyDigital helps you connect with farmers, merchants, and brokers securely. From market insights to real-time pricing and secure transactions, we’ve got you covered."
                    + "your password is : " + merchant.getPassword()
                    + "\nThank you for joining us!\n- The MyDigital Team";
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(merchant.getMEmail());
            message.setSubject("welcome ");
            message.setText(s);
            mailSender.send(message);
            otpMap.remove(email);
            session.removeAttribute("pendingMerchant");
            session.invalidate();
            return "MerchantLogin";
        } else {
            model.addAttribute("error", "Invalid OTP. Please try again.");
            model.addAttribute("email", email);
            return "VerifyOtp";
        }
    }

    private void sendOtpEmail(String toEmail, String otp) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(toEmail);
        helper.setSubject("OTP for Merchant Registration");
        String s = "Welcome to MyDigital! \n Thank you for registering with us.\nYour One-Time Password (OTP) for registration is: " + "\n" + otp + "\n"
                + "This OTP is valid for 10 minutes. Please enter it on the verification page to complete your registration."
                + "If you did not request this, please ignore this message.\nBest regards,\nThe MyDigital Team";
        helper.setText(s, true);
        mailSender.send(message);
    }

    // Merchant Login

    @GetMapping("/login")
    public String showLoginForm(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("merchantId") != null) {
            return "redirect:/Merchant/MerchantHome"; // Redirect if already logged in
        }
        return "MerchantLogin";
    }

    @PostMapping("/login")
    public String loginMerchant(@RequestParam String email, @RequestParam String password, Model model, HttpServletRequest request) {
        Optional<Merchant> merchant = merchantService.loginMerchant(email, password);

        if (merchant.isPresent()) {
            HttpSession session = request.getSession(true);
            session.setAttribute("merchantId", merchant.get().getId());
            session.setAttribute("merchantName", merchant.get().getMName());
            session.setAttribute("merchantEmail", merchant.get().getMEmail());
            session.setAttribute("payment", merchant.get().getMpayment());
            session.setAttribute("Total", merchant.get().getTotalpay());
            System.out.println(merchant.get().getTotalpay());
            return "redirect:/Merchant/MerchantHome";
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "MerchantLogin";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/Merchant/login";
    }

    // Merchant Home page
    @GetMapping("/MerchantHome")
    public String MerchantHomePage(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Long merchantId = null;
        if (session != null) {
            merchantId = (Long) session.getAttribute("merchantId");
        }

        if (merchantId != null) {
            model.addAttribute("merchantName", session.getAttribute("merchantName"));
            model.addAttribute("merchantEmail", session.getAttribute("merchantEmail"));
            model.addAttribute("products", productService.getProductsByMerchantId(merchantId));
            model.addAttribute("merchantId", session.getAttribute("merchantId"));
            model.addAttribute("payment", session.getAttribute("payment"));
            model.addAttribute("Total", session.getAttribute("Total"));
          //  model.addAttribute("commodities", scrapingService.getCommodities());
          //  model.addAttribute("data", scrapingService.scrapeTableData());
         //   model.addAttribute("weatherList", scrapingService.getWeatherData());
            return "MerchantHome";
        }
        return "redirect:/Merchant/login";
    }

    // All Merchants list
    @GetMapping("/merchants")
    public String listMerchants(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("validate") == null) {
            return "redirect:/Admin/AdminSign"; // Redirect to sign-in page if not logged in
        }

        model.addAttribute("merchants", merchantService.getAllMerchants());
        return "merchant_list";
    }

    // Merchant Profile Update By Admin
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Merchant> merchant = merchantService.getMerchantById(id);
        if (merchant.isPresent()) {
            model.addAttribute("merchant", merchant.get());
            return "MerchantUpdate";
        }
        return "redirect:/Merchant/merchants";
    }

    @PostMapping("/update/{id}")
    public String updateMerchant(@PathVariable Long id, @ModelAttribute Merchant updatedMerchant) {
        merchantService.updateMerchant(id, updatedMerchant);
        return "redirect:/Merchant/merchants";
    }

    // Merchant Profile Delete By Admin
    @PostMapping("/delete/{id}")
    public String deleteMerchant(@PathVariable Long id) {
        merchantService.deleteMerchant(id);
        return "redirect:/Merchant/merchants";
    }

    // Merchant Profile Update By Self
    @GetMapping("/self/edit/{id}")
    public String selfEditForm(@PathVariable Long id, Model model) {
        Optional<Merchant> merchant = merchantService.getMerchantById(id);
        if (merchant.isPresent()) {
            model.addAttribute("merchant", merchant.get());
            return "MerchantUpdateSelf";
        }
        return "redirect:/Merchant/MerchantHome";
    }

    @PostMapping("/self/update/{id}")
    public String selfupdateMerchant(@PathVariable Long id, @ModelAttribute Merchant updatedMerchant) {
        merchantService.updateMerchant(id, updatedMerchant);
        return "redirect:/Merchant/MerchantHome";
    }

    // Get Specific Merchant Products by the Id
    @GetMapping("/merchant/{merchantId}")
    public String getProductsByMerchant(@PathVariable Long merchantId, Model model) {
        Optional<Merchant> optionalMerchant = merchantService.getMerchantById(merchantId);
        if (optionalMerchant.isPresent()) {
            List<MProduct> products = productService.getProductsByMerchantId(merchantId);
            model.addAttribute("products", products);
            model.addAttribute("merchant", optionalMerchant.get());
            return "merchant-product-list";
        }
        return "error-page";
    }

    @GetMapping("/history/self")
    public String getMethodName(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Long merchantId = null;
        if (session != null) {
            merchantId = (Long) session.getAttribute("merchantId");
        }
        if (merchantId == null) {
            return "redirect:/Merchant/login";
        }
        model.addAttribute("products", (List<HMProduct>) HMproductService.getProductsByMerchantId(merchantId));
        return "merchant-product-history";
    }
}
