package com.datn.application.controller.admin;

import com.datn.application.config.PaypalPaymentIntent;
import com.datn.application.config.PaypalPaymentMethod;
import com.datn.application.service.PaypalService;
import com.datn.application.utils.PayPalUtil;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;


@Controller
public class PaymentController {
    public static final String URL_PAYPAL_SUCCESS = "pay/success";
    public static final String URL_PAYPAL_CANCEL = "pay/cancel";
    private Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private PaypalService paypalService;
//    @GetMapping("/payment")
//    public String index(){
//        return "shop/index";
//    }
    @PostMapping("/pay")
    public String pay(HttpServletRequest request, @RequestParam("price") double price ){
        String cancelUrl = PayPalUtil.getBaseURL(request) + "/" + URL_PAYPAL_CANCEL;
        String successUrl = PayPalUtil.getBaseURL(request) + "/" + URL_PAYPAL_SUCCESS;
        try {
            Payment payment = paypalService.createPayment(
                    price,
                    "USD",
                    PaypalPaymentMethod.paypal,
                    PaypalPaymentIntent.sale,
                    "payment description",
                    cancelUrl,
                    successUrl);
            for(Links links : payment.getLinks()){
                if(links.getRel().equals("approval_url")){
                    return "redirect:" + links.getHref();
                }
            }
        } catch (PayPalRESTException e) {
            log.error(e.getMessage());
        }
        return "shop/index";
    }
    @GetMapping(URL_PAYPAL_CANCEL)
    public String cancelPay(){
        return "cancel";
    }
    @GetMapping(URL_PAYPAL_SUCCESS)
    public String successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId){
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if(payment.getState().equals("approved")){
                return "shop/order_history";
            }
        } catch (PayPalRESTException e) {
            log.error(e.getMessage());
        }
        return "shop/order_history";
    }
}
