package sgu.ltudm.songssingersproject.models;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import sgu.ltudm.songssingersproject.encryptions.AESEncryption;
import sgu.ltudm.songssingersproject.encryptions.RSAEncryption;

import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Client {
    private static final int port = 1071;
    public static BufferedReader in;
    public static BufferedWriter out;
    public static SecretKey keyAES;
    private static String host;
    private static PublicKey publicKeyFromServer;
    private static PrivateKey myPrivateKey;

    public static void connectServer() throws Exception {
        try {
            host = getIPAddressServer();

            Socket socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // khóa công khai của Server
            String publicKeyStringFromServer = in.readLine();
            publicKeyFromServer = convertBytesToPublicKey(Base64.getDecoder().decode(publicKeyStringFromServer));

            // khóa đối xứng đã được mã hóa bằng khóa công khai do Server chia sẻ
            keyAES = AESEncryption.generateKey();
            String keyAESString = Base64.getEncoder().encodeToString(keyAES.getEncoded());
            String keyAESEncode = RSAEncryption.encrypt(keyAESString, publicKeyFromServer);

            // khóa công khai của Client để cho Server nhận, sau này Server mã hóa khóa đối xứng để gửi qua mạng
            KeyPair keyPair = RSAEncryption.generateKeyPair();
            PublicKey myPublicKey = keyPair.getPublic();
            String myPublicKeyString = Base64.getEncoder().encodeToString(myPublicKey.getEncoded());
            myPrivateKey = keyPair.getPrivate();

            out.write(myPublicKeyString);
            out.newLine();
            out.write(keyAESEncode);
            out.newLine();
            out.flush();
        } catch (Exception e) {
            throw new Exception("Không thể kết nối tới Server");
        }
    }

    private static PublicKey convertBytesToPublicKey(byte[] keyBytes) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // Thay "RSA" bằng thuật toán sử dụng
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return keyFactory.generatePublic(keySpec);
    }

    private static String getIPAddressServer() throws Exception {
        String ip = "";

        try {
            // https://retoolapi.dev/fHdCgZ/ipAddressServer
            String api = "https://retoolapi.dev/fHdCgZ/ipAddressServer/1";
            Document doc = Jsoup.connect(api).ignoreContentType(true).ignoreHttpErrors(true).header("Content-Type", "application/json").method(Connection.Method.GET).execute().parse();
            JSONObject jsonObject = new JSONObject(doc.text());

            ip = jsonObject.getString("ip");
        } catch (Exception e) {
            throw new Exception("Không thể lấy IP của Server");
        }

        return ip;
    }

    public static void stop() throws IOException {
        in.close();
        out.close();
    }
}

