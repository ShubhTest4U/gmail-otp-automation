# Gmail OTP Automation 🔐✨

> **Tired of manually copying OTPs from Gmail during automated tests?** This project solves that problem by automatically extracting OTP codes from your Gmail inbox using the Gmail API.

[![Java](https://img.shields.io/badge/Java-11%2B-orange.svg)](https://openjdk.java.net/)
[![Gmail API](https://img.shields.io/badge/Gmail%20API-v1-red.svg)](https://developers.google.com/gmail/api)
[![Playwright](https://img.shields.io/badge/Playwright-Java-green.svg)](https://playwright.dev/java/)

## What This Does

This tool automatically:
- 📧 Connects to your Gmail account securely
- 🔍 Searches for OTP emails from specific senders
- 🔢 Extracts 6-digit OTP codes using smart pattern matching
- 🤖 Integrates seamlessly with your test automation (Playwright, Selenium, etc.)
- ⏱️ Handles timing issues between OTP generation and email arrival

Perfect for **QA engineers**, **automation testers**, and **developers** who need to automate login flows that require email-based OTP verification.

## Quick Start

### Prerequisites

- ✅ Java 11 or higher
- ✅ Gmail account with API access enabled
- ✅ Google Cloud Project with Gmail API enabled

### 1. Clone & Setup

```bash
git clone https://github.com/ShubhTest4U/gmail-otp-automation.git
cd gmail-otp-automation
```

### 2. Gmail API Setup

1. **Create Google Cloud Project**
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create a new project or select existing one

2. **Enable Gmail API**
   - Navigate to "APIs & Services" → "Library"
   - Search for "Gmail API" and enable it

3. **Create OAuth2 Credentials**
   - Go to "APIs & Services" → "Credentials"
   - Click "Create Credentials" → "OAuth 2.0 Client IDs"
   - Choose "Desktop application"
   - Download the `credentials.json` file

4. **Place Credentials**
   ```bash
   # Move the downloaded file in your project root
   mv ~/Downloads/credentials.json ./credentials.json
   ```

### 3. Dependencies

Add these to your `pom.xml` (Maven) or `build.gradle` (Gradle):

```xml
<!-- Gmail API -->
<dependency>
    <groupId>com.google.apis</groupId>
    <artifactId>google-api-services-gmail</artifactId>
    <version>v1-rev20220404-1.32.1</version>
</dependency>
<dependency>
    <groupId>com.google.api-client</groupId>
    <artifactId>google-api-client</artifactId>
    <version>2.0.0</version>
</dependency>
<dependency>
    <groupId>com.google.oauth-client</groupId>
    <artifactId>google-oauth-client-jetty</artifactId>
    <version>1.34.1</version>
</dependency>

<!-- Playwright (for browser automation) -->
<dependency>
    <groupId>com.microsoft.playwright</groupId>
    <artifactId>playwright</artifactId>
    <version>1.40.0</version>
</dependency>
```

### 4. Basic Usage

```java
import Gmail_API.GmailOTPFetcher;

// Fetch the latest OTP from your Gmail
try {
    String otp = GmailOTPFetcher.fetchLatestOTP("me");
    System.out.println("Retrieved OTP: " + otp);
} catch (Exception e) {
    System.err.println("Failed to fetch OTP: " + e.getMessage());
}
```

## How It Works

### The Smart OTP Detection Process

1. **🔐 Secure Authentication**: Uses OAuth2 to securely connect to your Gmail
2. **🎯 Targeted Search**: Searches for emails with specific sender and subject patterns
3. **🔍 Content Parsing**: Extracts OTP using regex patterns from email HTML
4. **⚡ Smart Timing**: Waits for new OTPs and avoids returning stale codes
5. **🔄 Retry Logic**: Handles network issues and API rate limits gracefully

### Customization for Different Apps

The current implementation is configured for **Topmate.io** OTPs, but you can easily adapt it:

```java
// In GmailOTPFetcher.java, modify these variables:
String searchQuery = "from:your-app@example.com subject:Your OTP Subject";
String otpRegex = "your-otp-regex-pattern"; // Adjust based on email format
```

## Real-World Example

Here's how to integrate with a Playwright test:

```java
// 1. Start login process
page.navigate("https://yourapp.com/login");
page.fill("input[name='email']", "your-email@gmail.com");

// 2. Capture any existing OTP (to avoid stale codes)
String lastOtp = null;
try {
    lastOtp = GmailOTPFetcher.fetchLatestOTP("me");
} catch (Exception e) {
    // No previous OTP found - that's okay
}

// 3. Trigger new OTP
page.click("button[type='submit']");
page.waitForSelector("input[name='otp']");

// 4. Wait for new OTP (different from the last one)
String newOtp = waitForNewOTP(lastOtp);

// 5. Enter OTP and complete login
page.fill("input[name='otp']", newOtp);
page.click("button[name='verify']");
```

## Configuration

### Email Search Patterns

Modify `GmailOTPFetcher.java` to match your application's email format:

```java
// Example patterns for different services
String topmate = "from:support@topmate.io subject:OTP for Topmate login";
String auth0 = "from:noreply@auth0.com subject:verification code";
String custom = "from:noreply@yourapp.com subject:Login Code";
```

### OTP Extraction Patterns

Common regex patterns for different OTP formats:

```java
// 6-digit OTP in bold HTML
String boldPattern = "font-weight: bold[^>]*>(\\d{6})<";

// OTP in a specific div class
String divPattern = "<div class=\"otp-code\"[^>]*>(\\d{6})</div>";

// Plain text OTP
String plainPattern = "Your code is:?\\s*(\\d{6})";
```

## Troubleshooting

### Common Issues

**🚫 "No OTP email found"**
- Check if the search query matches your email format
- Verify the sender email address is correct
- Ensure the email subject contains expected keywords

**🚫 "OTP not found in recent emails"**  
- Update the regex pattern to match your email's HTML structure
- Check if the OTP is in a different part of the email
- Try expanding the search to more recent emails

**🚫 "Authentication failed"**
- Ensure `credentials.json` is in the correct location
- Check if Gmail API is enabled in Google Cloud Console
- Verify OAuth2 consent screen is configured

**🚫 "Rate limit exceeded"**
- Implement exponential backoff in your retry logic
- Consider caching tokens to reduce authentication calls
- Check your Google Cloud Console quotas

## Security Best Practices

- 🔒 **Never commit `credentials.json`** to version control
- 🔑 Use environment variables for sensitive configuration
- 🎯 Use minimal Gmail API scopes (`GMAIL_READONLY`)
- 🔄 Regularly rotate your OAuth2 credentials
- 🏢 Consider using service accounts for production environments

## Contributing

We welcome contributions! Here's how:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/feature_name`
3. Commit changes: `git commit -m 'Add feature_name'`
4. Push to branch: `git push origin feature/feature_name`
5. Open a Pull Request

### Ideas for Contributions

- 🌐 Support for more email providers (Outlook, Yahoo)
- 📱 SMS OTP integration
- 🔧 Configuration file support
- 🧪 Unit tests and integration tests
- 📚 Support for more programming languages

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

- 📖 **Documentation Issues**: Open a GitHub issue
- 💡 **Feature Requests**: Create an issue with the "enhancement" label  
- 🐛 **Bug Reports**: Use the bug report template
- 💬 **Questions**: Check existing issues or start a discussion

---

**Made with ❤️ by the automation community**

*Simplifying OTP automation, one test at a time.*
