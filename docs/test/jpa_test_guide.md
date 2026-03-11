# JPA 엔티티 테스트 코드 작성 가이드 (Fixture 패턴)

이 문서는 **JPA 엔티티를 Builder 패턴으로 생성하면서 테스트 코드를 작성하는 방법**을 정리한 가이드입니다.

**목표:**
- 엔티티 코드를 테스트 코드 때문에 오염시키지 않는다 (순수성 유지)
- 테스트에서 엔티티 생성이 쉽도록 만든다 (생산성)
- 테스트 코드 중복을 줄이고 가독성을 높인다

---

## 1. 기본 원칙
JPA 엔티티는 **도메인 모델 역할만 수행**하며, 테스트 전용 로직을 포함하지 않습니다.
- `@Id`는 DB(Strategy)에 위임하며, **생성자/빌더에서 제외**하여 불변성을 유지합니다.
- `withId()`, `setId()` 등 테스트만을 위한 변경 메서드를 만들지 않습니다.

---

## 2. 엔티티 작성 방식
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Builder
    private Member(String name) { // ID를 제외한 빌더
        this.name = name;
    }
}

```

---

## 3. Fixture 클래스 작성 (핵심)

테스트용 객체 생성은 `src/test` 패키지의 Fixture 클래스가 전담합니다.

```java
public class MemberFixture {

    public static Member member() {
        return memberBuilder().build();
    }

    // ID가 필요한 Mocking/단위 테스트용
    public static Member member(Long id) {
        Member member = memberBuilder().build();
        // Reflection을 통해 private 필드인 id 강제 주입
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    // 필드 커스텀이 필요한 경우를 위한 빌더 노출
    public static Member.MemberBuilder memberBuilder() {
        return Member.builder()
                .name("test-user");
    }
}

```

---

## 4. 테스트 코드 활용 예시

### 단위 테스트 (ID 필요 시)

```java
@Test
void 회원_정보_수정_테스트() {
    // Given
    Member member = MemberFixture.member(1L); // ID 1번 객체 생성
    
    // When & Then ...
}

```

### 복합 객체 생성 (연관관계)

```java
public class PostFixture {
    public static Post post(Member writer) {
        return Post.builder()
                .title("제목")
                .content("내용")
                .writer(writer) // 타 Fixture에서 생성된 객체 주입
                .build();
    }
}

```
