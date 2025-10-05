package com.expensesplitter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Service for handling currency conversion and formatting.
 * Provides currency conversion rates and utilities.
 */
public class CurrencyService {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);
    
    // Mock exchange rates - in a real implementation, these would come from an API
    private static final Map<String, BigDecimal> EXCHANGE_RATES = new HashMap<>();
    
    static {
        // Base currency USD = 1.0
        EXCHANGE_RATES.put("USD", new BigDecimal("1.0"));
        EXCHANGE_RATES.put("EUR", new BigDecimal("0.85"));
        EXCHANGE_RATES.put("GBP", new BigDecimal("0.73"));
        EXCHANGE_RATES.put("JPY", new BigDecimal("110.0"));
        EXCHANGE_RATES.put("AUD", new BigDecimal("1.35"));
        EXCHANGE_RATES.put("CAD", new BigDecimal("1.25"));
        EXCHANGE_RATES.put("CHF", new BigDecimal("0.92"));
        EXCHANGE_RATES.put("CNY", new BigDecimal("6.45"));
        EXCHANGE_RATES.put("INR", new BigDecimal("74.5"));
    }
    
    private static final Set<String> SUPPORTED_CURRENCIES = EXCHANGE_RATES.keySet();
    
    /**
     * Converts an amount from one currency to another
     */
    public BigDecimal convertCurrency(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (amount == null || fromCurrency == null || toCurrency == null) {
            throw new IllegalArgumentException("Amount and currencies cannot be null");
        }
        
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }
        
        if (!SUPPORTED_CURRENCIES.contains(fromCurrency)) {
            throw new IllegalArgumentException("Unsupported from currency: " + fromCurrency);
        }
        
        if (!SUPPORTED_CURRENCIES.contains(toCurrency)) {
            throw new IllegalArgumentException("Unsupported to currency: " + toCurrency);
        }
        
        // Convert to USD first, then to target currency
        BigDecimal fromRate = EXCHANGE_RATES.get(fromCurrency);
        BigDecimal toRate = EXCHANGE_RATES.get(toCurrency);
        
        // Convert from source currency to USD
        BigDecimal amountInUSD = amount.divide(fromRate, 6, RoundingMode.HALF_UP);
        
        // Convert from USD to target currency
        BigDecimal convertedAmount = amountInUSD.multiply(toRate);
        
        return convertedAmount.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Gets the current exchange rate between two currencies
     */
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        if (fromCurrency == null || toCurrency == null) {
            throw new IllegalArgumentException("Currencies cannot be null");
        }
        
        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE;
        }
        
        BigDecimal fromRate = EXCHANGE_RATES.get(fromCurrency);
        BigDecimal toRate = EXCHANGE_RATES.get(toCurrency);
        
        if (fromRate == null || toRate == null) {
            throw new IllegalArgumentException("Unsupported currency");
        }
        
        return toRate.divide(fromRate, 6, RoundingMode.HALF_UP);
    }
    
    /**
     * Gets all supported currencies
     */
    public Set<String> getSupportedCurrencies() {
        return new HashSet<>(SUPPORTED_CURRENCIES);
    }
    
    /**
     * Formats currency amount with proper symbol
     */
    public String formatCurrency(BigDecimal amount, String currency) {
        if (amount == null || currency == null) {
            return "N/A";
        }
        
        String symbol = getCurrencySymbol(currency);
        return String.format("%s%.2f", symbol, amount);
    }
    
    /**
     * Gets currency symbol for a currency code
     */
    public String getCurrencySymbol(String currencyCode) {
        switch (currencyCode.toUpperCase()) {
            case "USD": return "$";
            case "EUR": return "€";
            case "GBP": return "£";
            case "JPY": return "¥";
            case "AUD": return "A$";
            case "CAD": return "C$";
            case "CHF": return "CHF ";
            case "CNY": return "¥";
            case "INR": return "₹";
            default: return currencyCode + " ";
        }
    }
    
    /**
     * Gets currency display name
     */
    public String getCurrencyDisplayName(String currencyCode) {
        switch (currencyCode.toUpperCase()) {
            case "USD": return "US Dollar";
            case "EUR": return "Euro";
            case "GBP": return "British Pound";
            case "JPY": return "Japanese Yen";
            case "AUD": return "Australian Dollar";
            case "CAD": return "Canadian Dollar";
            case "CHF": return "Swiss Franc";
            case "CNY": return "Chinese Yuan";
            case "INR": return "Indian Rupee";
            default: return currencyCode;
        }
    }
    
    /**
     * Gets a list of popular currencies for dropdown menus
     */
    public List<String> getPopularCurrencies() {
        return Arrays.asList("USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "INR");
    }
    
    /**
     * Validates if a currency code is supported
     */
    public boolean isCurrencySupported(String currencyCode) {
        return SUPPORTED_CURRENCIES.contains(currencyCode.toUpperCase());
    }
    
    /**
     * Updates exchange rates (in a real app, this would fetch from an API)
     */
    public void updateExchangeRates() {
        logger.info("Updating exchange rates...");
        // In a real implementation, this would fetch from a currency API
        // For now, we'll just log that it was called
        logger.info("Exchange rates updated successfully");
    }
    
    /**
     * Gets the timestamp of last update (mock implementation)
     */
    public String getLastUpdateTimestamp() {
        return "2024-10-05 17:50:00 UTC";
    }
}