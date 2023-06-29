package swith.backend.service;

import org.springframework.data.domain.Page;
import swith.backend.domain.Post;
import swith.backend.domain.PostSearch;
import swith.backend.dto.PostInfoDto;
import swith.backend.dto.PostUpdateDto;


public interface PostService {
    /**
     * 게시글 저장
     */
    void register(Post post);

    /**
     * 게시글 수정
     */
    void update(Long id, PostUpdateDto postUpdateDto);

    /**
     * 게시글 삭제
     */
    void delete(Long id);

    /**
     * 게시글 1개 조회
     */
    PostInfoDto getPostInfo(Long id);

    /**
     * 게시글 페이징 조회
     */
    public Page<Post> getPostList(int page,int size);

    /**
     * 전체 게시글 개수와 페이지 조회
     */
    public Page<Post> getPageList(int size);

    /**
     * 검색 기능을 활용한 페이지 조회
     */
    public Page<Post> PostSearch(PostSearch postSearch,int page,int size);
}
