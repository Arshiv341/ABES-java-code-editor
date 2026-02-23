package DSAtraing;

import java.util.*;

public class anshi {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter Array size: ");
        int n = sc.nextInt();

        System.out.print("Enter kth value: ");
        int k = sc.nextInt();

        int[] arr = new int[n];

        System.out.println("Enter Array elements:");
        for(int i = 0; i < n; i++){
            arr[i] = sc.nextInt();
        }

        // Sliding Window
        for(int i = 0; i <= n - k; i++) {

            boolean found = false;

            for(int j = i; j < i + k; j++) {
                if(arr[j] < 0) {
                    System.out.print(arr[j] + " ");
                    found = true;
                    break;
                }
            }

            if(!found) {
                System.out.print("0 ");
            }
        }
    }
}