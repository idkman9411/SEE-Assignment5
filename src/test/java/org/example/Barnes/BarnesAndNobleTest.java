package org.example.Barnes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;


class BarnesAndNobleTest {
/**
 * 1.Input/output
 * -Input: bookDatabase, BuyBookProcess
 * -Output: PurchaseSummary
 *
 * getPriceForCart
 * -Input: Map<String,Integer>
 * -Output: PurchaseSummary
 *
 * 2.Partitions
 * A.Single
 *
 * BookDatabase: Empty | has isbn | !isbn
 * BuyBookProcess: quantity is >= amount| quantity < amount
 *
 * Map: null | empty | single | multiple
 *
 * Combination:
 * map is null, BD is Empty, BBP is >= amount
 * map !null, BD is Empty, BBP is < amount
 * map is null, BD has Isbn, BBP is >= amount
 * map !null, BD has isbn , BBP is < amount
 *
 * map is empty, BD is Empty, BBP is >= amount
 * map !empty, BD is Empty, BBP is < amount
 * map is empty, BD has Isbn, BBP is >= amount
 * map is empty, BD has isbn , BBP is < amount
 *
 *
 *  map !empty, BD has isbn, BBP is >= amount
 *  map !empty, BD has isbn, BBP is < amount
 *  map !empty, BD !isbn, BBP is >= amount
 *  map !empty, BD !isbn, BBP is < amount
 *
 *  Test Cases:
 *  Map null, empty, >= -> null
 *  null, !empty, >= -> null
 *
 *  empty, empty, >= -> 0
 *  [123->2,456->4], empty, >= -> Throws
 *  [123->2,456->4], not isbn, >= -> Throws
 *
 *  [123->2,456->4], has 123, >=  ->
 *  [123->2,456->4], has 456, >= ->
 * [123->2,456->4], has both, >= -> sum of both
 *
 * [123->2,456->4], has both,  amount 0 of 123, -> sum of 456
 * [123->2,456->4], has both, amount < 123 -> sum of 456 and 1 123
 * [123->2,456->4], has both, amount 0 0f 456 -> sum of 123
 * [123->2,456->4], has both, amount < 456, -> sum of 123 and 2 456
 *
 */


Book pennyDred = new Book("123",3,2);
Book comic = new Book("456",7,4);
Book history = new Book("789",10,5);

Book fantasy = new Book("123",4,0);
Book memoir = new Book("456", 6, 0);

@DisplayName("specification-based")
@Test
    public void testPriceNull(){
    //map is null, BD has Isbn, BBP is >= amount
    BookDatabase bd1 = mock(BookDatabase.class);
    BuyBookProcess bbp1 = mock(BuyBookProcess.class);

    when(bd1.findByISBN("123")).thenReturn(pennyDred);
    when(bd1.findByISBN("456")).thenReturn(comic);

    //verify(bbp,times(1)).buyBook(pennyDred,2);
    //verify(bbp,times(1)).buyBook(comic,4);

    BarnesAndNoble bAndN1 = new BarnesAndNoble(bd1,bbp1);
    assertNull(bAndN1.getPriceForCart(null));

    //map is null, BD is Empty, BBP is >= amount
    PurchaseSummary ps2 = new PurchaseSummary();// before each
    BookDatabase bd2 = mock(BookDatabase.class);
    BuyBookProcess bbp2 = mock(BuyBookProcess.class);

    BarnesAndNoble bAndN2 = new BarnesAndNoble(bd2,bbp2);
    assertNull(bAndN2.getPriceForCart(null));

    }


    @DisplayName("specification-based")
    @Test
    public void testPriceEmpty(){
        //empty, empty, >= -> 0
        BookDatabase bd1 = mock(BookDatabase.class);
        BuyBookProcess bbp1 = mock(BuyBookProcess.class);

        HashMap<String,Integer> order1 = new HashMap<>();
        BarnesAndNoble bandn1 = new BarnesAndNoble(bd1,bbp1);
        assertEquals(0,bandn1.getPriceForCart(order1).getTotalPrice());

        // no test or catch on findByISBN call
        //[123->2,456->4], empty, >= -> Throws
        HashMap<String,Integer> order2 = new HashMap<>();
        order2.put("123",2);
        order2.put("456",4);

        BarnesAndNoble bandn2 = new BarnesAndNoble(bd1,bbp1);
        assertThrows(NullPointerException.class,()->{bandn2.getPriceForCart(order2);});
    }

    @DisplayName("specification-based")
    @Test
    public void testRetrieveBookNoISBN(){
        // no test or catch on findByISBN call
        //[123->2,456->4], not isbn, >= -> Throws
        BookDatabase bd = mock(BookDatabase.class);
        BuyBookProcess bbp = mock(BuyBookProcess.class);
        when(bd.findByISBN("789")).thenReturn(history);

        HashMap<String,Integer> order = new HashMap<>();
        order.put("123",2);
        order.put("456",4);

        BarnesAndNoble bandn = new BarnesAndNoble(bd,bbp);
        assertThrows(NullPointerException.class,()->{bandn.getPriceForCart(order);});
    }

    @DisplayName("specification-based")
    @Test
    public void testRetrieve(){
        //[123->2,456->4], has 123, >=  ->
        BookDatabase bd1 = mock(BookDatabase.class);
        BuyBookProcess bbp1 = mock(BuyBookProcess.class);
        when(bd1.findByISBN("123")).thenReturn(pennyDred);
        when(bd1.findByISBN("789")).thenReturn(history);

        HashMap<String,Integer> order1 = new HashMap<>();
        order1.put("123",2);
        order1.put("456",4);
        BarnesAndNoble bandn1 = new BarnesAndNoble(bd1,bbp1);
        assertThrows(NullPointerException.class,()->{bandn1.getPriceForCart(order1);});
        verify(bbp1,times(1)).buyBook(pennyDred,2);

        //[123->2,456->4], has both, >= -> sum of both
        BookDatabase bd2 = mock(BookDatabase.class);
        BuyBookProcess bbp2 = mock(BuyBookProcess.class);
        when(bd2.findByISBN("123")).thenReturn(pennyDred);
        when(bd2.findByISBN("456")).thenReturn(comic);

        HashMap<String,Integer> order2 = new HashMap<>();
        order2.put("123",2);
        order2.put("456",4);
        BarnesAndNoble bandn2 = new BarnesAndNoble(bd2,bbp2);
        assertEquals(34,bandn2.getPriceForCart(order2).getTotalPrice());
    }

    @DisplayName("specification-based")
    @Test
    public void testUnavailableOfOne(){
       //[123->2,456->4], has both,  amount 0 of 123, -> sum of 456
        BookDatabase bd1 = mock(BookDatabase.class);
        BuyBookProcess bbp1 = mock(BuyBookProcess.class);
        when(bd1.findByISBN("123")).thenReturn(pennyDred);
        when(bd1.findByISBN("456")).thenReturn(memoir);

        HashMap<String,Integer> order1 = new HashMap<>();
        order1.put("123",2);
        order1.put("456",4);
        HashMap<Book,Integer> hold = new HashMap<>();
        hold.put(memoir,4);
        BarnesAndNoble bandn1 = new BarnesAndNoble(bd1,bbp1);
        PurchaseSummary res = bandn1.getPriceForCart(order1);
        assertEquals(6,res.getTotalPrice());
        assertEquals(hold,res.getUnavailable());
    }

    @DisplayName("specification-based")
    @Test
    public void testUnavailableLessThan(){
        // [123->2,456->4], has both, amount < 123 -> sum of 456 and 1 1
        BookDatabase bd1 = mock(BookDatabase.class);
        BuyBookProcess bbp1 = mock(BuyBookProcess.class);
        when(bd1.findByISBN("123")).thenReturn(pennyDred);
        Book placeh = new Book("456",7,1);
        when(bd1.findByISBN("456")).thenReturn(placeh);

        HashMap<String,Integer> order1 = new HashMap<>();
        order1.put("123",2);
        order1.put("456",4);
        HashMap<Book,Integer> hold = new HashMap<>();
        hold.put(placeh,3);

        BarnesAndNoble bandn1 = new BarnesAndNoble(bd1,bbp1);
        PurchaseSummary res = bandn1.getPriceForCart(order1);
        assertEquals(13,res.getTotalPrice());
        assertEquals(hold,res.getUnavailable());
    }

    @DisplayName("structural-based")
    @Test
    public void testBook(){
        Book placeHold = null;
        String num = "456";
        assertTrue(comic.equals(new Book("456",7,4)));
        assertTrue(comic.equals(comic));
        assertFalse(comic.equals(placeHold));
        assertFalse(comic.equals(num));
    }

}