/*
 * Copyright (C) 2018-2024 University of Waterloo.
 *
 * This file is part of Perses.
 *
 * Perses is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3, or (at your option) any later version.
 *
 * Perses is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Perses; see the file LICENSE.  If not see <http://www.gnu.org/licenses/>.
 */
package org.pluverse.jvm.fuzz.util

class FuzzerUtilStubTemplate(
  val seed: Long,
) {
  fun generateCode(): String {
    return """
      package org.pluverse.jvm.fuzz;

      import java.util.Random;
      
      class FuzzerUtils {
        public static Random random = new Random(${seed}L);


        // Array initialization

        // boolean -----------------------------------------------
        public static void init(boolean[] a, boolean seed) {
            for (int j = 0; j < a.length; j++) {
                a[j] = (j % 2 == 0) ? seed : (j % 3 == 0);
            }
        }

        public static void init(boolean[][] a, boolean seed) {
            for (int j = 0; j < a.length; j++) {
                init(a[j], seed);
            }
        }

        // long --------------------------------------------------
        public static void init(long[] a, long seed) {
            for (int j = 0; j < a.length; j++) {
                a[j] = (j % 2 == 0) ? seed + j : seed - j;
            }
        }

        public static void init(long[][] a, long seed) {
            for (int j = 0; j < a.length; j++) {
                init(a[j], seed);
            }
        }

        // int --------------------------------------------------
        public static void init(int[] a, int seed) {
            for (int j = 0; j < a.length; j++) {
                a[j] = (j % 2 == 0) ? seed + j : seed - j;
            }
        }

        public static void init(int[][] a, int seed) {
            for (int j = 0; j < a.length; j++) {
                init(a[j], seed);
            }
        }

        // short --------------------------------------------------
        public static void init(short[] a, short seed) {
            for (int j = 0; j < a.length; j++) {
                a[j] = (short) ((j % 2 == 0) ? seed + j : seed - j);
            }
        }

        public static void init(short[][] a, short seed) {
            for (int j = 0; j < a.length; j++) {
                init(a[j], seed);
            }
        }

        // char --------------------------------------------------
        public static void init(char[] a, char seed) {
            for (int j = 0; j < a.length; j++) {
                a[j] = (char) ((j % 2 == 0) ? seed + j : seed - j);
            }
        }

        public static void init(char[][] a, char seed) {
            for (int j = 0; j < a.length; j++) {
                init(a[j], seed);
            }
        }

        // byte --------------------------------------------------
        public static void init(byte[] a, byte seed) {
            for (int j = 0; j < a.length; j++) {
                a[j] = (byte) ((j % 2 == 0) ? seed + j : seed - j);
            }
        }

        public static void init(byte[][] a, byte seed) {
            for (int j = 0; j < a.length; j++) {
                init(a[j], seed);
            }
        }

        // double --------------------------------------------------
        public static void init(double[] a, double seed) {
            for (int j = 0; j < a.length; j++) {
                a[j] = (j % 2 == 0) ? seed + j : seed - j;
            }
        }

        public static void init(double[][] a, double seed) {
            for (int j = 0; j < a.length; j++) {
                init(a[j], seed);
            }
        }

        // float --------------------------------------------------
        public static void init(float[] a, float seed) {
            for (int j = 0; j < a.length; j++) {
                a[j] = (j % 2 == 0) ? seed + j : seed - j;
            }
        }

        public static void init(float[][] a, float seed) {
            for (int j = 0; j < a.length; j++) {
                init(a[j], seed);
            }
        }

        // boolean -----------------------------------------------
        public static long checkSum(boolean[] a) {
            long sum = 0;
            for (int j = 0; j < a.length; j++) {
                sum += (a[j] ? j + 1 : 0);
            }
            return sum;
        }

        public static long checkSum(boolean[][] a) {
            long sum = 0;
            for (int j = 0; j < a.length; j++) {
                sum += checkSum(a[j]);
            }
            return sum;
        }

        // long --------------------------------------------------
        public static long checkSum(long[] a) {
            long sum = 0;
            for (int j = 0; j < a.length; j++) {
                sum += (a[j] / (j + 1) + a[j] % (j + 1));
            }
            return sum;
        }

        public static long checkSum(long[][] a) {
            long sum = 0;
            for (int j = 0; j < a.length; j++) {
                sum += checkSum(a[j]);
            }
            return sum;
        }

        // int --------------------------------------------------
        public static long checkSum(int[] a) {
            long sum = 0;
            for (int j = 0; j < a.length; j++) {
                sum += (a[j] / (j + 1) + a[j] % (j + 1));
            }
            return sum;
        }

        public static long checkSum(int[][] a) {
            long sum = 0;
            for (int j = 0; j < a.length; j++) {
                sum += checkSum(a[j]);
            }
            return sum;
        }

        // short --------------------------------------------------
        public static long checkSum(short[] a) {
            long sum = 0;
            for (int j = 0; j < a.length; j++) {
                sum += (short) (a[j] / (j + 1) + a[j] % (j + 1));
            }
            return sum;
        }

        public static long checkSum(short[][] a) {
            long sum = 0;
            for (int j = 0; j < a.length; j++) {
                sum += checkSum(a[j]);
            }
            return sum;
        }

        // char --------------------------------------------------
        public static long checkSum(char[] a) {
            long sum = 0;
            for (int j = 0; j < a.length; j++) {
                sum += (char) (a[j] / (j + 1) + a[j] % (j + 1));
            }
            return sum;
        }

        public static long checkSum(char[][] a) {
            long sum = 0;
            for (int j = 0; j < a.length; j++) {
                sum += checkSum(a[j]);
            }
            return sum;
        }

        // byte --------------------------------------------------
        public static long checkSum(byte[] a) {
            long sum = 0;
            for (int j = 0; j < a.length; j++) {
                sum += (byte) (a[j] / (j + 1) + a[j] % (j + 1));
            }
            return sum;
        }

        public static long checkSum(byte[][] a) {
            long sum = 0;
            for (int j = 0; j < a.length; j++) {
                sum += checkSum(a[j]);
            }
            return sum;
        }

        // double --------------------------------------------------
        public static double checkSum(double[] a) {
            double sum = 0;
            for (int j = 0; j < a.length; j++) {
                sum += (a[j] / (j + 1) + a[j] % (j + 1));
            }
            return sum;
        }

        public static double checkSum(double[][] a) {
            double sum = 0;
            for (int j = 0; j < a.length; j++) {
                sum += checkSum(a[j]);
            }
            return sum;
        }

        // float --------------------------------------------------
        public static double checkSum(float[] a) {
            double sum = 0;
            for (int j = 0; j < a.length; j++) {
                sum += (a[j] / (j + 1) + a[j] % (j + 1));
            }
            return sum;
        }

        public static double checkSum(float[][] a) {
            double sum = 0;
            for (int j = 0; j < a.length; j++) {
                sum += checkSum(a[j]);
            }
            return sum;
        }

        // Object --------------------------------------------------
        public static long checkSum(Object[][] a) {
            long sum = 0;
            for (int j = 0; j < a.length; j++) {
                sum += checkSum(a[j]);
            }
            return sum;
        }

        public static long checkSum(Object[] a) {
            long sum = 0;
            for (int j = 0; j < a.length; j++) {
                sum += checkSum(a[j]) * Math.pow(2, j);
            }
            return sum;
        }

        public static long checkSum(Object a) {
            if (a == null)
                return 0L;
            return (long) a.getClass().getCanonicalName().length();
        }

        // Array creation ------------------------------------------
        public static byte[] byte1array(int sz, byte seed) {
            byte[] ret = new byte[sz];
            init(ret, seed);
            return ret;
        }

        public static byte[][] byte2array(int sz, byte seed) {
            byte[][] ret = new byte[sz][sz];
            init(ret, seed);
            return ret;
        }

        public static short[] short1array(int sz, short seed) {
            short[] ret = new short[sz];
            init(ret, seed);
            return ret;
        }

        public static short[][] short2array(int sz, short seed) {
            short[][] ret = new short[sz][sz];
            init(ret, seed);
            return ret;
        }

        public static int[] int1array(int sz, int seed) {
            int[] ret = new int[sz];
            init(ret, seed);
            return ret;
        }

        public static int[][] int2array(int sz, int seed) {
            int[][] ret = new int[sz][sz];
            init(ret, seed);
            return ret;
        }

        public static long[] long1array(int sz, long seed) {
            long[] ret = new long[sz];
            init(ret, seed);
            return ret;
        }

        public static long[][] long2array(int sz, long seed) {
            long[][] ret = new long[sz][sz];
            init(ret, seed);
            return ret;
        }

        public static float[] float1array(int sz, float seed) {
            float[] ret = new float[sz];
            init(ret, seed);
            return ret;
        }

        public static float[][] float2array(int sz, float seed) {
            float[][] ret = new float[sz][sz];
            init(ret, seed);
            return ret;
        }

        public static double[] double1array(int sz, double seed) {
            double[] ret = new double[sz];
            init(ret, seed);
            return ret;
        }

        public static double[][] double2array(int sz, double seed) {
            double[][] ret = new double[sz][sz];
            init(ret, seed);
            return ret;
        }

        public static char[] char1array(int sz, char seed) {
            char[] ret = new char[sz];
            init(ret, seed);
            return ret;
        }

        public static char[][] char2array(int sz, char seed) {
            char[][] ret = new char[sz][sz];
            init(ret, seed);
            return ret;
        }

        public static boolean[] boolean1array(int sz, boolean seed) {
            boolean[] ret = new boolean[sz];
            init(ret, seed);
            return ret;
        }

        public static boolean[][] boolean2array(int sz, boolean seed) {
            boolean[][] ret = new boolean[sz][sz];
            init(ret, seed);
            return ret;
        }
      }

    """.trimIndent()
  }
}
