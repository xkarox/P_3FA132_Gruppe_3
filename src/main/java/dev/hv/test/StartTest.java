package dev.hv.test;

public class StartTest {

   public static int getJavaMainVersion() {

      final String[] versionElements = getJavaVersion().split("\\.");
      final int discard = Integer.parseInt(versionElements[0]);
      int version;
      if (discard == 1) {
         // 1.8 !
         version = Integer.parseInt(versionElements[1]);
      } else {
         version = discard;
      }
      return version;
   }

   public static String getJavaVersion() {
      return System.getProperty("java.version");
   }

   public static void main(final String[] args) {
      System.out.format("Java-Runtime-Version: %s%n", getJavaVersion());
   }

}
