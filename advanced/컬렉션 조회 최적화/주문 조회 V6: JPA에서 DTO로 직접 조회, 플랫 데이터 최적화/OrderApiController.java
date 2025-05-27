package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.*;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;


/**
 * V1. 엔티티 직접 노출
 * - 엔티티가 변하면 API 스펙이 변한다.
 * - 트랜잭션 안에서 지연 로딩 필요
 * - 양방향 연관관계 문제
 *
 * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
 * - 트랜잭션 안에서 지연 로딩 필요
 * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
 * - 페이징 시에는 N 부분을 포기해야함(대신에 batch fetch size? 옵션 주면 N -> 1 쿼리로 변경 가능)
 *
 * V4. JPA에서 DTO로 바로 조회, 컬렉션 N 조회 (1 + N Query)
 * - 페이징 가능
 * V5. JPA에서 DTO로 바로 조회, 컬렉션 1 조회 최적화 버전 (1 + 1 Query)
 * - 페이징 가능
 * V6. JPA에서 DTO로 바로 조회, 플랫 데이터(1Query) (1 Query)
 * - 페이징 불가능...
 *
 */

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     *  V1. 엔티티 직접 노출
     *  - 엔티티가 변하면 API 스펙이 변한다.
     *  - 트랜잭션 안에서 지연 로딩 필요
     *  - 양방향 연관관계 문제
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            // 원래는 지연 로딩이라 이하 코드 없이 실행하면 orderItems 결과가 null로 뜨는데
            // 아래처럼 강제 초기화 해주어서 지연 로딩 설정하였음에도 null로 뜨지 않게 함
            // 물론 이 때 양방향 연관 관계 있는 엔티티들은 어느 한 쪽에 @JsonIgnore 해주어야 함
            List<OrderItem> orderItems = order.getOrderItems();  // 프록시 초기화
            orderItems.stream().forEach(o -> o.getItem().getName());  // 아래 코드를 lambda 형식으로 변경
//            for (OrderItem orderItem : orderItems) {
//                orderItem.getItem().getName();  // orderItem 내의 item 들도 초기화
//            }
        }
        return all;
    }

    /**
     *  V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
     *  - 트랜잭션 안에서 지연 로딩 필요
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return collect;
    }

    /**
     * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
     *  - 페이징 시에는 N 부분을 포기해야함(대신에 batch fetch size? 옵션 주면 N -> 1 쿼리로 변경 가능)
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        for (Order order : orders) {
            System.out.println("order ref = "+order+"id="+order.getId());
        }
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                        @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    @GetMapping("/api/v6/orders")
    public List<OrderFlatDto> ordersV6() {
//    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
        return orderQueryRepository.findAllByDto_flat();
        // 만약 OrderQueryDto 타입으로 리턴하고 싶다면, 내가 아래 코드처럼직접 중복을 거르면 됨
//        return flats.stream()
//                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
//                        mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
//                )).entrySet().stream()
//                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress()))
//                .collect(toList());
    }

    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        // 엔티티와의 관계를 완전히 끊기 위함
        private List<OrderItemDto> orderItems;
        // private List<OrderItem> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            // 아래 코드로 돌리면 orderItems는 나오지 않음. 엔티티이기 때문임
//            orderItems = order.getOrderItems();
            // 그러나 아래 코드를 추가하여 프록시를 초기화하면, orderItems 도 출력됨
            // 그러나 이하 코드는 DTO에 엔티티가 들어가 있으므로 엔티티가 외부에 노출되어 있는 것으로 볼 수 있음
//            order.getOrderItems().stream().forEach(o -> o.getItem().getName());
            // 엔티티와의 관계를 완전 끊는 코드는 아래와 같음
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(toList());
        }
    }

    @Data
    static class OrderItemDto {
        // 고객이 필요한 이하 3개의 데이터만 포함시키기
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}

//@RestController
//@RequiredArgsConstructor
//public class OrderApiController {
//
//    private final OrderRepository orderRepository;
//    private final OrderQueryRepository orderQueryRepository;
//
//    /**
//     * V1. 엔티티 직접 노출
//     * - Hibernate5Module 모듈 등록, LAZY=null 처리
//     * - 양방향 관계 문제 발생 -> @JsonIgnore
//     */
//    @GetMapping("/api/v1/orders")
//    public List<Order> ordersV1() {
//        List<Order> all = orderRepository.findAll();
//        for (Order order : all) {
//            order.getMember().getName(); //Lazy 강제 초기화
//            order.getDelivery().getAddress(); //Lazy 강제 초기환
//            List<OrderItem> orderItems = order.getOrderItems();
//            orderItems.stream().forEach(o -> o.getItem().getName()); //Lazy 강제 초기화
//        }
//        return all;
//    }
//
//    @GetMapping("/api/v2/orders")
//    public List<OrderDto> ordersV2() {
//        List<Order> orders = orderRepository.findAll();
//        List<OrderDto> result = orders.stream()
//                .map(o -> new OrderDto(o))
//                .collect(toList());
//
//        return result;
//    }
//
//    @GetMapping("/api/v3/orders")
//    public List<OrderDto> ordersV3() {
//        List<Order> orders = orderRepository.findAllWithItem();
//        List<OrderDto> result = orders.stream()
//                .map(o -> new OrderDto(o))
//                .collect(toList());
//
//        return result;
//    }
//
//    /**
//     * V3.1 엔티티를 조회해서 DTO로 변환 페이징 고려
//     * - ToOne 관계만 우선 모두 페치 조인으로 최적화
//     * - 컬렉션 관계는 hibernate.default_batch_fetch_size, @BatchSize로 최적화
//     */
//    @GetMapping("/api/v3.1/orders")
//    public List<OrderDto> ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
//                                        @RequestParam(value = "limit", defaultValue = "100") int limit) {
//
//        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
//        List<OrderDto> result = orders.stream()
//                .map(o -> new OrderDto(o))
//                .collect(toList());
//
//        return result;
//    }
//
//    @GetMapping("/api/v4/orders")
//    public List<OrderQueryDto> ordersV4() {
//        return orderQueryRepository.findOrderQueryDtos();
//    }
//
//    @GetMapping("/api/v5/orders")
//    public List<OrderQueryDto> ordersV5() {
//        return orderQueryRepository.findAllByDto_optimization();
//    }
//
//    @GetMapping("/api/v6/orders")
//    public List<OrderQueryDto> ordersV6() {
//        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
//
//        return flats.stream()
//                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
//                        mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
//                )).entrySet().stream()
//                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
//                .collect(toList());
//    }
//
//    @Data
//    static class OrderDto {
//
//        private Long orderId;
//        private String name;
//        private LocalDateTime orderDate; //주문시간
//        private OrderStatus orderStatus;
//        private Address address;
//        private List<OrderItemDto> orderItems;
//
//        public OrderDto(Order order) {
//            orderId = order.getId();
//            name = order.getMember().getName();
//            orderDate = order.getOrderDate();
//            orderStatus = order.getStatus();
//            address = order.getDelivery().getAddress();
//            orderItems = order.getOrderItems().stream()
//                    .map(orderItem -> new OrderItemDto(orderItem))
//                    .collect(toList());
//        }
//    }
//
//    @Data
//    static class OrderItemDto {
//
//        private String itemName;//상품 명
//        private int orderPrice; //주문 가격
//        private int count;      //주문 수량
//
//        public OrderItemDto(OrderItem orderItem) {
//            itemName = orderItem.getItem().getName();
//            orderPrice = orderItem.getOrderPrice();
//            count = orderItem.getCount();
//        }
//    }
//
//}
