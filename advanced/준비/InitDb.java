package jpabook.jpashop;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

/**
 * 종 주문 2개
 * * userA
 * 	 * JPA1 BOOK
 * 	 * JPA2 BOOK
 * * userB
 * 	 * SPRING1 BOOK
 * 	 * SPRING2 BOOK
 */
@Component
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;

    public void init() {
        initService.dbInit1();
    }

    // 이하 로직을 위의 init() 함수에 넣어도 될 것 같으나, 실제로는 스프링 라이프사이클 때문에 init() 함수에 넣으면 안 됨
    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {
        private final EntityManager em;
        public void dbInit1() {
            Member member = createMember("userA", "서울", "1", "111111");
            Book book1 = createBook("JPA1 BOOK", 10000, 100);
            Book book2 = createBook("JPA2 BOOK", 20000, 100);
            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);
            Delivery delivery = createDelivery(member);
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }

        private Delivery createDelivery(Member member) {
            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            return delivery;
        }

        private Book createBook(String name, int price, int stockQuantity) {
            Book book1 = new Book();
            book1.setName(name);
            book1.setPrice(price);
            book1.setStockQuantity(stockQuantity);
            em.persist(book1);
            return book1;
        }

        private Member createMember(String userA, String 서울, String s, String s2) {
            Member member = new Member();
            member.setName(userA);
            member.setAddress(new Address(서울, s, s2));
            em.persist(member);  // em을 영속성 상태로 만듦
            return member;
        }

        public void dbInit2() {
            Member member = createMember("userB", "진주", "2", "222222");
            Book book1 = createBook("SPRING1 BOOK", 20000, 200);
            Book book2 = createBook("SPRING2 BOOK", 40000, 400);
            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 20000, 3);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 40000, 4);
            Delivery delivery = createDelivery(member);
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }
    }
}





//public class InitDb {
//
//    private final InitService initService;
//
//    @PostConstruct
//    public void init() {
//        initService.dbInit1();
//        initService.dbInit2();
//    }
//
//    @Component
//    @Transactional
//    @RequiredArgsConstructor
//    static class InitService {
//
//        private final EntityManager em;
//
//        public void dbInit1() {
//            System.out.println("Init1" + this.getClass());
//            Member member = createMember("userA", "서울", "1", "1111");
//            em.persist(member);
//
//            Book book1 = createBook("JPA1 BOOK", 10000, 100);
//            em.persist(book1);
//
//            Book book2 = createBook("JPA2 BOOK", 20000, 100);
//            em.persist(book2);
//
//            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
//            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);
//
//            Delivery delivery = createDelivery(member);
//            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
//            em.persist(order);
//        }
//
//        public void dbInit2() {
//            Member member = createMember("userB", "진주", "2", "2222");
//            em.persist(member);
//
//            Book book1 = createBook("SPRING1 BOOK", 20000, 200);
//            em.persist(book1);
//
//            Book book2 = createBook("SPRING2 BOOK", 40000, 300);
//            em.persist(book2);
//
//            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 20000, 3);
//            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 40000, 4);
//
//            Delivery delivery = createDelivery(member);
//            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
//            em.persist(order);
//        }
//
//        private Member createMember(String name, String city, String street, String zipcode) {
//            Member member = new Member();
//            member.setName(name);
//            member.setAddress(new Address(city, street, zipcode));
//            return member;
//        }
//
//        private Book createBook(String name, int price, int stockQuantity) {
//            Book book1 = new Book();
//            book1.setName(name);
//            book1.setPrice(price);
//            book1.setStockQuantity(stockQuantity);
//            return book1;
//        }
//
//        private Delivery createDelivery(Member member) {
//            Delivery delivery = new Delivery();
//            delivery.setAddress(member.getAddress());
//            return delivery;
//        }
//    }
//}

