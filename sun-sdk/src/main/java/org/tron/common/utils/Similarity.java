package org.tron.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Similarity {

  public String cmd;
  public int largestCommonSeq;
  public int minEditDist;

  public Similarity(String cmd, int lcs, int med) {
    this.cmd = cmd;
    largestCommonSeq = lcs;
    minEditDist = med;
  }

  public static int findLCS(String word1, String word2) {
    int dp[][] = new int[word1.length() + 1][word2.length() + 1];

    for (int i = 0; i < word1.length() + 1; i++) {
      dp[i][0] = 0;
    }
    for (int j = 0; j < word2.length() + 1; j++) {
      dp[0][j] = 0;
    }

    for (int i = 1; i <= word1.length(); i++) {
      for (int j = 1; j <= word2.length(); j++) {
        if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
          dp[i][j] = dp[i - 1][j - 1] + 1;
        } else {
          dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
        }
      }
    }
    return dp[word1.length()][word2.length()];
  }

  public static int minDistance(String word1, String word2) {
    int dp[][] = new int[word1.length() + 1][word2.length() + 1];

    for (int i = 0; i < word1.length() + 1; i++) {
      dp[i][0] = i;
    }
    for (int j = 0; j < word2.length() + 1; j++) {
      dp[0][j] = j;
    }

    for (int i = 1; i < word1.length() + 1; i++) {
      for (int j = 1; j < word2.length() + 1; j++) {
        if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
          dp[i][j] = dp[i - 1][j - 1];
        } else {
          dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
        }
      }
    }
    return dp[word1.length()][word2.length()];
  }

  public static List<String> getSimilarWordList(String input, List<String> cmds) {
    List<String> ret = new ArrayList<String>();
    List<Similarity> list = new ArrayList<Similarity>();

    for (String cmd : cmds) {
      int lcs = findLCS(input, cmd);
      if (lcs > 0) {
        int med = minDistance(input, cmd);
        list.add(new Similarity(cmd, lcs, med));
      }
    }

    Collections.sort(list, new Comparator<Similarity>() {
      public int compare(Similarity e1, Similarity e2) {
        if (e2.largestCommonSeq == e1.largestCommonSeq) {
          return e1.minEditDist - e2.minEditDist;
        } else {
          return e2.largestCommonSeq - e1.largestCommonSeq;
        }
      }
    });

    Iterator<Similarity> it = list.iterator();
    while (it.hasNext()) {
      ret.add(it.next().cmd);
      if (ret.size() >= 5) {
        break;
      }
    }

    return ret;
  }

}
