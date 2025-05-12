package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

// 강의에서 나오는 에러들이 나는 포스트맨이 아니라 서버 로그에 찍히므로 주의!!

/**
 * @RestController
 *  - Spring MVC에서 Controller 역할을 하며, 메서드의 반환 값을 자동으로 JSON이나 XML 형식으로 변환
 *  - @RestController = @Controller + @ResponseBody
 *  - @Controller: 해당 클래스가 HTTP 요청을 처리하는 컨트롤러
 *  - @ResponseBody: 메서드의 반환 값을 JSON 등의 HTTP Response Body로 바로 내려줌
 *
 * @RequiredArgsConstructor
 *  - final이나 @NonNull이 붙은 필드를 포함하는 생성자를 자동으로 만들어주는 Lombok 어노테이션
 *  - private final MemberService memberService; -> 컴파일 시 아래 생성자가 자동으로 생성
 *  - public MemberApiController(MemberService memberService) {
 *     this.memberService = memberService;
 * }
 * */
@RestController
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;
    /**
     * @RequestBody
     *  - HTTP 요청의 Body(JSON, XML 등)를 Java 객체로 변환하기 위한 어노테이션
     *  - 클라이언트로부터 전송된 JSON 요청 바디를 Member 객체로 자동 매핑
     *  - 요청 헤더에 Content-Type: application/json이 있어야 정상 동작
     * @Valid
     *  - Bean Validation을 적용하여 객체의 필드 값을 검증하는 어노테이션
     *  - @RequestBody로 바인딩된 객체의 필드 값이 적절한지 검사
     * */
    /**
     * 회원 등록 V1: 요청 값으로 Member 엔티티를 직접 받는다.
     * 문제점
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     *   - 엔티티에 API 검증을 위한 로직이 들어간다. (@NotEmpty 등등)
     *   - 실무에서는 회원 엔티티를 위한 API가 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 모든 요청 요구사항을 담기는 어렵다.
     * - 엔티티가 변경되면 API 스펙이 변한다. -> 즉 name을 username으로 바꾸면 api 자체를 사용 불가
     * 결론 (해결 방법)
     * - API 요청 스펙에 맞추어 별도의 DTO를 파라미터로 받는다. : saveMemberV2 로 구현
     * - 따라서 실무에서 개발할 때는 1) 절대 엔티티를 파라미터로 받거나 웹에 노출하지마 2) 중간에 DTO 만들어서 정보 받아
     */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);  // 회원 가입 메서드
        return new CreateMemberResponse(id);
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * 회원 수정
     */
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        // 회원 수정 API에서는 Member 엔티티 전체를 반환하지 않음.
        // 쿼리와 커맨드를 분리하기 위해!
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    /**
     * 회원 조회 v1: member의 정보 전체(엔티티)를 반환
     *  - 회원 등록 V1에서 발생하는 문제가 재발생
     *  - 번외 문제: 리턴되는 전체 엔티티 정보에 count 같은 다른 정보를 넣을 수 없음
     */
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result membersV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());
        return new Result(collect.size(), collect);  // 껍데기가 엔티티 [] 에서 리스트 {}로 바뀌어서 리턴됨
    }

    /**
     * @Data
     *  - Java에서 DTO나 Entity 클래스를 만들 때 자주 작성하게 되는 보일러플레이트 코드를 자동으로 생성하는 Lombok 어노테이션
 *      (getter, setter, toString, equals, hashCode, 생성자)
 *      - 영한쌤은 엔티티(@Entity) 에서는 어노테이션 잘 안 쓰심. BUT DTO에서는 상대적으로 어노테이션 막 쓰심
     */
    // 이 파일 내에서만 사용할거니까 별도의 request, response 폴더 및 파일 만들지 않고 여기서 설정
    @Data
    @AllArgsConstructor
    static class Result<T> {
        // 추가 정보 요청하면 엔티티에 없는 count 같은 정보 아래처럼 바로 추가 가능
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    static class CreateMemberRequest {
        @NotEmpty  // 이제 DTO에 이러한 제약들 맘껏 설정하면 됨
        // 위 조건을 설정했는데 name을 null로 하여 요청 보내면 MethodArgumentNotValidException
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;
        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

}