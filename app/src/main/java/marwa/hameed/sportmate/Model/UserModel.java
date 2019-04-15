package marwa.hameed.sportmate.Model;

public class UserModel
{
    String name,email,imageurl,mobile,gender;

    public UserModel() {
    }

    public UserModel(String name, String email, String imageurl, String mobile, String gender) {
        this.name = name;
        this.email = email;
        this.imageurl = imageurl;
        this.mobile = mobile;
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
