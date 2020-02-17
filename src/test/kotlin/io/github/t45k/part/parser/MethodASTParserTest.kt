package io.github.t45k.part.parser

import org.eclipse.jdt.core.dom.MethodDeclaration
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals

internal class MethodASTParserTest {

    @Test
    fun testParseBinarySearch() {
        val contents = String(Files.readAllBytes(Paths.get("src/test/resources/sample/methods/CollectionsBinarySearch.mjava")))
        val binarySearch: MethodDeclaration = MethodASTParser.parse(contents)
        assertEquals("""private static <T>int indexedBinarySearch(List<? extends Comparable<? super T>> list,T key){
  int low=0;
  int high=list.size() - 1;
  while (low <= high) {
    int mid=(low + high) >>> 1;
    Comparable<? super T> midVal=list.get(mid);
    int cmp=midVal.compareTo(key);
    if (cmp < 0)     low=mid + 1;
 else     if (cmp > 0)     high=mid - 1;
 else     return mid;
  }
  return -(low + 1);
}
""", binarySearch.toString())
    }

    @Test
    fun testParseMathMax() {
        val contents = String(Files.readAllBytes(Paths.get("src/test/resources/sample/methods/MathMax.mjava")))
        val mathMax: MethodDeclaration = MethodASTParser.parse(contents)
        assertEquals("""public static int max(int a,int b){
  return a >= b ? a : b;
}
""", mathMax.toString())
    }
}