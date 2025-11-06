package org.example.Amazon;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.example.Amazon.Cost.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

class AmazonUnitTest {
    /**
     * Input/Output
     * -Input: Shopping Cart, List<PriceRule>
     *      addToCart: Item
     *
     *  -Output:
     *      calculate: double
     *
     *  Partitions
     *  A. Single
     *  Shopping Cart: null | empty | has items
     *  List:<PriceRule>: null | empty | no rule apply | some rules apply | all rules apply
     *
     *  Item: null | item
     *
     *  B. Combination
     *  Amazon:
     *  Cart null, List null -> Calculate + addToCart throws
     *  empty, null -> throws, item added
     *  null, empty ->  throws
     *
     *  empty,empty -> 0 , item added
     *  !empty, empty -> 0, item added
     *
     *  empty, no rule apply -> 0, item added
     *  has items, no rule apply -> 0, item added
     *  empty, some rules apply -> double, item added
     *  has items, some rules apply -> double, item added
     *  empty, all rules apply -> double, item added
     *  has items, all rules apply -> double, item added
     *
     *  addToCart:
     *  item null -> throws
     *  item -> item added
     *
     *  Test cases:
     *  null, null -> throws null pointer excpetion
     *  empty, empty ->0, item added
     *  empty, null -> throws, item added
     *
     *  empty,[Delivery,Electronic,Regular] -> 0, item added
     *  tea,[Electronic,Delivery] -> 5, item added
     *  tea, [Electronic] -> 0, item added
     *  tea,pencils,[Electronic,Delivery,Regular], -> 3+, item added
     *  tea,pencils,earbuds,[Electronic,Delivery,Regular] ->12.50+, item added
     *
     *
     */

    Item tea = new Item(ItemType.OTHER,"pack of tea",2,5.00);
    Item pencil = new Item(ItemType.OTHER,"10 pack of pencils",4,3.00);
    Item earbuds = new Item(ItemType.ELECTRONIC,"par of earbuds",1,20);

    @DisplayName("specification-based")
    @Test
    public void testNull(){
        Amazon ama = new Amazon(null,null);
        assertThrows(NullPointerException.class, ()->ama.addToCart(tea));
        assertThrows(NullPointerException.class, ()->ama.calculate());

        ShoppingCart cart = mock(ShoppingCart.class);
        when(cart.getItems()).thenReturn(List.of(tea));
        Amazon ama2 = new Amazon(cart,null);
        assertThrows(NullPointerException.class, ()->ama2.calculate());
        ama2.addToCart(tea);
        verify(cart,times(1)).add(tea);
    }

    @DisplayName("specification-based")
    @Test
    public void testEmpty(){
        ShoppingCart cart = mock(ShoppingCart.class);
        when(cart.getItems()).thenReturn(List.of(tea));
        List<PriceRule> rules = new ArrayList<PriceRule>();
        Amazon ama = new Amazon(cart, rules);
        ama.addToCart(tea);

        verify(cart,times(1)).add(tea);
        assertEquals(0,ama.calculate());

        ShoppingCart cart2 = mock(ShoppingCart.class);
        //when(cart2.getItems()).thenReturn(List.of(tea));
        rules.add(new DeliveryPrice());
        rules.add(new ExtraCostForElectronics());
        rules.add(new RegularCost());
        Amazon ama2 = new Amazon(cart2, rules);
        assertEquals(0,ama2.calculate());
    }


    @DisplayName("specification-based")
    @Test
    public void testNoRuleApply(){
        ShoppingCart cart = mock(ShoppingCart.class);
        when(cart.getItems()).thenReturn(List.of(tea));
        List<PriceRule> rules = new ArrayList<PriceRule>();
        rules.add(new ExtraCostForElectronics());

        Amazon ama = new Amazon(cart, rules);
        ama.addToCart(tea);
        assertEquals(0,ama.calculate());
    }


    @DisplayName("specification-based")
    @Test
    public void testOneRuleApply(){
        ShoppingCart cart = mock(ShoppingCart.class);
        when(cart.getItems()).thenReturn(List.of(tea));
        List<PriceRule> rules = new ArrayList<PriceRule>();
        rules.add(new ExtraCostForElectronics());
        rules.add(new DeliveryPrice());

        Amazon ama = new Amazon(cart, rules);
        ama.addToCart(tea);
        assertEquals(5,ama.calculate());
    }

    @DisplayName("specification-based")
    @Test
    public void testRules(){
        ShoppingCart cart = mock(ShoppingCart.class);
        when(cart.getItems()).thenReturn(List.of(tea,pencil));
        List<PriceRule> rules = new ArrayList<PriceRule>();
        rules.add(new ExtraCostForElectronics());
        rules.add(new DeliveryPrice());
        rules.add(new RegularCost());

        Amazon ama = new Amazon(cart, rules);
        ama.addToCart(tea);
        ama.addToCart(pencil);
        assertEquals(22+5,ama.calculate());

        when(cart.getItems()).thenReturn(List.of(tea,pencil,earbuds));
        ama.addToCart(earbuds);
        assertEquals(54.5,ama.calculate());

        verify(cart,times(1)).add(tea);
        verify(cart,times(1)).add(pencil);
        verify(cart,times(1)).add(earbuds);

    }

    @DisplayName("structural-based")
    @Test
    public void testDelivery() { //has some always true conditions
        ShoppingCart cart = mock(ShoppingCart.class);
        when(cart.getItems()).thenReturn(List.of());
        List<PriceRule> rules = new ArrayList<>();
        rules.add(new DeliveryPrice());

        Amazon ama = new Amazon(cart, rules);
        assertEquals(0,ama.calculate());

        Item phonecase = new Item(ItemType.OTHER, "galaxy phonecase", 1, 10);
        Item belt = new Item(ItemType.OTHER, "belt", 1, 1);
        Item bat = new Item(ItemType.OTHER, "bat", 1, 10);
        Item toy = new Item(ItemType.OTHER, "toy", 1, 10);
        Item koi = new Item(ItemType.OTHER, "koi", 1, 10);
        Item ball = new Item(ItemType.OTHER, "ball", 1, 10);
        Item mug = new Item(ItemType.OTHER, "mug", 1, 10);
        Item tie = new Item(ItemType.OTHER, "tie", 1, 10);


        when(cart.getItems()).thenReturn(List.of(tea, pencil, phonecase, bat, belt, toy, earbuds));
        assertEquals(12.5, ama.calculate());

        when(cart.getItems()).thenReturn(List.of(tea, pencil, phonecase, bat, belt, toy, earbuds, koi, ball, mug, tie));
        assertEquals(20.0, ama.calculate());
    }

    @DisplayName("structural-based")
    @Test
    public void testItem(){
        assertEquals("pack of tea",tea.getName());
        assertEquals(2,tea.getQuantity());
        assertEquals(5.00,tea.getPricePerUnit());
        assertEquals(ItemType.OTHER,tea.getType());
    }
}