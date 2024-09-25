package dev.hv.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StartTestTest {

   @Test
   void testJavaVersion() {
      assertEquals(21, StartTest.getJavaMainVersion(), "Version must be 21!");
   }

}
