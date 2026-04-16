package DJ.MyDigital.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InfoCropController {

    // Home page
    @GetMapping("/info")
    public String home() {
        return "infoHome"; // Make sure 'index.html' exists in templates
    }

    // Crops
    @GetMapping("/barley")
    public String barley() {
        return "barley";
    }

    @GetMapping("/maize")
    public String maize() {
        return "maize";
    }

    @GetMapping("/rice")
    public String rice() {
        return "rice";
    }

    @GetMapping("/soybean")
    public String soybean() {
        return "soybean";
    }

    @GetMapping("/cotton")
    public String cotton() {
        return "cotton";
    }

    @GetMapping("/sugarcane")
    public String sugarcane() {
        return "sugarcane";
    }

    // Fruits
    @GetMapping("/grapes")
    public String grapes() {
        return "grapes";
    }

    @GetMapping("/pomegranate")
    public String pomegranate() {
        return "pomegranate";
    }

    @GetMapping("/banana")
    public String banana() {
        return "banana";
    }

    @GetMapping("/mango")
    public String mango() {
        return "mango";
    }

    @GetMapping("/guava")
    public String guava() {
        return "guava";
    }

    @GetMapping("/watermelon")
    public String watermelon() {
        return "watermelon";
    }

    // Flowers
    @GetMapping("/rose")
    public String rose() {
        return "rose";
    }

    @GetMapping("/marigold")
    public String marigold() {
        return "marigold";
    }

    @GetMapping("/jasmine")
    public String jasmine() {
        return "jasmine";
    }

    // Vegetables
    @GetMapping("/brinjal")
    public String brinjal() {
        return "brinjal";
    }

    @GetMapping("/potato")
    public String potato() {
        return "potato";
    }

    @GetMapping("/chilli")
    public String chilli() {
        return "chilli";
    }

    @GetMapping("/tomato")
    public String tomato() {
        return "tomato";
    }

    @GetMapping("/spinach")
    public String spinach() {
        return "spinach";
    }

    @GetMapping("/carrot")
    public String carrot() {
        return "carrot";
    }

    // Medicinal Plants
    @GetMapping("/aloevera")
    public String aloevera() {
        return "aloevera";
    }

    @GetMapping("/amla")
    public String amla() {
        return "amla";
    }

    @GetMapping("/ashwagandha")
    public String ashwagandha() {
        return "ashwagandha";
    }
}
