package campusshop;

import java.time.MonthDay;

/** 用户。 */
public record User(String username, String password, String address, String phone,
                   MemberLevel level, MonthDay birthday) {

    /** 普通会员。 */
    public User(String username, String password, String address, String phone) {
        this(username, password, address, phone, MemberLevel.NORMAL, null);
    }

    /** 指定会员等级，没有生日资料。 */
    public User(String username, String password, String address, String phone, MemberLevel level) {
        this(username, password, address, phone, level, null);
    }
}
