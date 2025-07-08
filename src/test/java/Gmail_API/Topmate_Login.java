package Gmail_API;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;

public class Topmate_Login {

	public static void main(String[] args) {
		try {
			Playwright playwright = Playwright.create();
			Browser browser = playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(false));
			Page page = browser.newPage();

			page.navigate("https://topmate.io/sign-in", new Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD));
			page.getByPlaceholder("Email").fill("Test@gmail.com");

			// Get the last OTP before triggering new OTP
			String lastOtp = null;
			try {
				lastOtp = GmailOTPFetcher.fetchLatestOTP("me");
			} catch (Exception e) {
				// Ignore the exception if no OTP found yet
			}

			page.locator("xpath=//button[normalize-space()='Continue']").click();
			page.waitForSelector("input[aria-label^='OTP Input']");

			String otp = null;
			int maxAttempts = 10; // Max number of attempts to fetch the new OTP.
			int attempt = 0; //// Current attempt count.
			
			// Keep trying to fetch the OTP until it's found or max attempts are reached.
			while (otp == null && attempt < maxAttempts) {
			    try {
			    	// Attempt to fetch the latest OTP from Gmail.
			        String candidateOtp = GmailOTPFetcher.fetchLatestOTP("me");
			        
			        //Check if the fetched OTP is different from the 'lastOtp' (before triggering)
			        if (lastOtp == null || !candidateOtp.equals(lastOtp)) {
			            otp = candidateOtp; 
			        } else {
			        	// If it's the same as the old OTP, wait a bit and retry.
			            Thread.sleep(1000); // wait 1 second before retry
			        }
			    } catch (Exception e) {
			        Thread.sleep(1000); 
			    }
			    attempt++;
			}
			if (otp == null) {
			    throw new Exception("OTP not found after waiting for new OTP");
			}
			System.out.println("OTP fetched: " + otp);

			//Enter the fetched OTP digit by digit into the input box.
			for (int i = 0; i < otp.length(); i++) {
				String selector = String.format("input[aria-label='OTP Input %d']", i + 1);
				page.locator(selector).fill(Character.toString(otp.charAt(i)));
			}

			page.locator("xpath=//button[normalize-space()='Login']").click();
			System.out.println("Redirect successfully on homepage");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
