package in.duplicatevoter.finder;

final class LiveVoterService {
    static final String OFFICIAL_VOTER_SERVICES_URL = "https://voters.eci.gov.in/";

    private LiveVoterService() {
    }

    static boolean hasApprovedApiConfiguration() {
        return false;
    }

    static String integrationStatus() {
        return "Official ECI API credentials are not configured. Use the ECI portal link for live verification.";
    }
}
