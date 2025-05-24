package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}

	@Bean
	Hibernate5Module hibernate5Module() {
		Hibernate5Module hibernate5Module = new Hibernate5Module();
		//강제 지연 로딩 설정
		// 위처럼 설정하면 지연 로딩 설정되어있던 Member 정보도 강제로 가지고 온다
		// 그러나 위처럼 처리하면 엔티티를 외부에 노출하여 엔티티 바뀌면 api 스펙이 바뀌어버림
		// 또한 api에서 구체적으로 필요하지 않은 데이터까지 조회하게 되어 성능 저하됨
//		hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);
		return hibernate5Module;
	}}
