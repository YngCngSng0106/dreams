package com.dreamshare.controller;

import com.dreamshare.dto.SearchResponse;
import com.dreamshare.dto.TagResponse;
import com.dreamshare.service.SearchService;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@CrossOrigin
public class SearchController {

    @Autowired private SearchService searchService;

    @GetMapping
    public Result<SearchResponse> search(@RequestParam String keyword,
                                          @RequestParam(required = false) String type,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(searchService.search(keyword, type, page, pageSize));
    }

    @GetMapping("/tags")
    public Result<List<TagResponse>> tags(@RequestParam String keyword) {
        return Result.ok(searchService.searchTags(keyword));
    }
}
