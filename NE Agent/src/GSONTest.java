import com.google.gson.Gson;

public class GSONTest {

    public GSONTest() {
        Gson gson = new Gson();
        int[] arr = {1, 2, 3};
        System.out.println(gson.toJson(arr));
    }

    public static void main(String[] args) {
        GSONTest gt = new GSONTest();
    }
}
