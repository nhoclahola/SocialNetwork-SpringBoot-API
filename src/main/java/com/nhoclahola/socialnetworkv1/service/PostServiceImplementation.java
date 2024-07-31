package com.nhoclahola.socialnetworkv1.service;

import com.nhoclahola.socialnetworkv1.dto.post.request.PostCreateRequest;
import com.nhoclahola.socialnetworkv1.dto.post.response.PostResponse;
import com.nhoclahola.socialnetworkv1.entity.Post;
import com.nhoclahola.socialnetworkv1.entity.User;
import com.nhoclahola.socialnetworkv1.exception.AppException;
import com.nhoclahola.socialnetworkv1.exception.ErrorCode;
import com.nhoclahola.socialnetworkv1.mapper.PostMapper;
import com.nhoclahola.socialnetworkv1.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImplementation implements PostService
{
    private final PostRepository postRepository;
    private final UserService userService;
    private final PostMapper postMapper;


    // Absolute path in project
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
    private static final String UPLOAD_POST_DIR = UPLOAD_DIR + "/posts/";
    private final VideoUploadServiceImplementation videoUploadServiceImplementation;
    private final ImageUploadServiceImplementation imageUploadServiceImplementation;

    @Override
    @Transactional
    public String createNewPost(String caption, MultipartFile image, MultipartFile video) throws IOException
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUserByEmail(currentUserEmail);
        String imageUrl = null;
        String videoUrl = null;
        if (!image.isEmpty())
            imageUrl = imageUploadServiceImplementation.upload(currentUser.getUserId(), UPLOAD_POST_DIR, image);
        if (!video.isEmpty())
            videoUrl = videoUploadServiceImplementation.upload(currentUser.getUserId(), UPLOAD_POST_DIR, video);
        Post newPost = Post.builder()
                .caption(caption)
                .imageUrl(imageUrl)
                .videoUrl(videoUrl)
                .user(currentUser)
                .createdAt(LocalDateTime.now())
                .build();
        postRepository.save(newPost);
        return "Created post successfully";
    }

    @Override
    public String deletePost(String postId)
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUserByEmail(currentUserEmail);
        Post post = postRepository.findById(postId).orElseThrow(() ->
                new AppException(ErrorCode.POST_NOT_EXIST));
        if (post.getUser() != currentUser)
            throw new RuntimeException("You can't delete this post");
        postRepository.delete(post);
        return "Post have been deleted successful";
    }

    @Override
    public List<PostResponse> findPostByUserId(String userId)
    {
        List<Post> posts = postRepository.findPostByUserId(userId);
        return postMapper.toListPostResponse(posts);
    }

    @Override
    public Post findPostById(String postId)
    {
        return postRepository.findById(postId).orElseThrow(() ->
                new AppException(ErrorCode.POST_NOT_EXIST));
    }

    @Override
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<PostResponse> findAllPosts()
    {
        List<Post> posts = postRepository.findAll();
        return postMapper.toListPostResponse(posts);
    }

    @Override
    @Transactional
    public String savePost(String postId)
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUserByEmail(currentUserEmail);
        Post post = this.findPostById(postId);
        if (post.getSaved().contains(currentUser))
        {
            post.getSaved().remove(currentUser);
            postRepository.save(post);
            return "You just removed this post to your saved posts list";
        }
        else
        {
            post.getSaved().add(currentUser);
            postRepository.save(post);
            return "You just added this post to your saved posts list";
        }
    }

    @Override
    @Transactional
    public String likePost(String postId)
    {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUserByEmail(currentUserEmail);
        Post post = this.findPostById(postId);
        if (post.getLiked().contains(currentUser))
        {
            post.getLiked().remove(currentUser);
            postRepository.save(post);
            return "You just unliked this post";
        }
        else
        {
            post.getLiked().add(currentUser);
            postRepository.save(post);
            return "You just liked this post";
        }
    }

    @Override
    public PostResponse findPostByIdResponse(String postId)
    {
        Post post = postRepository.findById(postId).orElseThrow(() ->
                new AppException(ErrorCode.POST_NOT_EXIST));
        return postMapper.toPostResponse(post);
    }
}
