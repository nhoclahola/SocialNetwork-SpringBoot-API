package com.nhoclahola.socialnetworkv1.dto.user.response;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserResponse
{
    private String userId;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private Boolean gender;
    private String avatarUrl;
    private String coverPhotoUrl;
}
