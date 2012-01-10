import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class AdsHostsMerger {
    public static void main(String args[]) throws IOException {
        disableHttpsCheck();
        Set<String> hosts = new TreeSet<String>();
        downloadYoyo(hosts);
        System.out.println("yoyo: " + hosts.size());
        downloadAdblock(hosts, "https://adblock-chinalist.googlecode.com/svn/trunk/adblock.txt");
        System.out.println("chinalist: " + hosts.size());
        downloadAdblock(hosts, "https://ruadlist.googlecode.com/svn/trunk/advblock.txt");
        System.out.println("russia: " + hosts.size());
        downloadAdblock(hosts, "https://easylist-downloads.adblockplus.org/easylistgermany+easylist.txt");
        System.out.println("germany: " + hosts.size());
        downloadAdblock(hosts, "https://easylist-downloads.adblockplus.org/liste_fr+easylist.txt");
        System.out.println("france: " + hosts.size());
        downloadAdblock(hosts, "https://easylist-downloads.adblockplus.org/rolist+easylist.txt");
        System.out.println("ro: " + hosts.size());
        downloadAdblock(hosts, "https://dutchadblockfilters.googlecode.com/svn/trunk/AdBlock_Dutch_hide.txt");
        System.out.println("nether: " + hosts.size());
        downloadAdblock(hosts, "https://easylist-downloads.adblockplus.org/bulgarian_list+easylist.txt");
        System.out.println("bu: " + hosts.size());
        downloadAdblock(hosts, "https://easylist-downloads.adblockplus.org/abpindo+easylist.txt");
        System.out.println("in: " + hosts.size());
        downloadAdblock(hosts, "https://secure.fanboy.co.nz/fanboy-adblock.txt");
        System.out.println("fanboy: " + hosts.size());
        String hostsFile = System.getenv("TMP") + "\\" + "deny-hosts.txt";
        System.out.println("hosts: " + hostsFile);
        FileWriter writer = new FileWriter(hostsFile);
        for (String host : hosts) {
            writer.write("127.0.0.1\t"+host);
            writer.write('\n');
        }
        writer.close();
    }

    private static void disableHttpsCheck() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }
        } };
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
    }

    private static void downloadAdblock(Set<String> hosts, String urlString) throws MalformedURLException, IOException {
        URL url = new URL(urlString);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.matches("^.*[a-z].*$") && line.indexOf('*') == -1 && line.indexOf('/') == -1 && line.startsWith("||") && line.endsWith("^")) {
                String host = line.substring(2, line.length() - 1);
                hosts.add(host);
            }
        }
        reader.close();
    }

    private static void downloadYoyo(Set<String> hosts) throws MalformedURLException, IOException {
        URL url = new URL("http://pgl.yoyo.org/adservers/serverlist.php?hostformat=hosts&showintro=1&startdate[day]=&startdate[month]=&startdate[year]=");
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        String line;
        boolean started = false;
        while ((line = reader.readLine()) != null) {
            if (line.contains("</pre>")) {
                started = false;
            }
            if (started && line.indexOf('#') == -1 && line.length() > 10) {
                hosts.add(line.substring(10));
            }
            if (line.contains("<pre>")) {
                started = true;
            }
        }
        reader.close();
    }
}