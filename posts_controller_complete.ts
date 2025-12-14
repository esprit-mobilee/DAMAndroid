import {
    Body,
    Controller,
    Delete,
    Get,
    Param,
    Post,
    Put,
    Req,
    UploadedFile,
    UseGuards,
    UseInterceptors,
    ForbiddenException,
} from '@nestjs/common';
import { PostsService } from './posts.service';
import { CreatePostDto } from './dto/create-post.dto';
import { UpdatePostDto } from './dto/update-post.dto';
import { FileInterceptor } from '@nestjs/platform-express';
import { multerOptions } from 'src/common/multer.config';
import { AuthenticationGuard } from 'src/auth/guards/authentication.guard';
import { IsPresidentGuard } from 'src/auth/guards/is-president.guard';
import { Role } from 'src/auth/enums/role.enum';

@Controller('posts')
@UseGuards(AuthenticationGuard)
export class PostsController {
    constructor(private readonly postsService: PostsService) { }

    // PRESIDENT: create post
    @Post(':clubId')
    @UseGuards(IsPresidentGuard)
    @UseInterceptors(FileInterceptor('image', multerOptions('posts')))
    async create(
        @Param('clubId') clubId: string,
        @Body() dto: CreatePostDto,
        @UploadedFile() file: Express.Multer.File,
        @Req() req: any,
    ) {
        const user = req.user;
        const managedClubId = user.club ?? user.presidentOf;
        const isAdmin = user.role === Role.Admin;

        if (!isAdmin && (!managedClubId || String(managedClubId) !== clubId)) {
            throw new ForbiddenException(
                'Vous ne pouvez publier que dans votre propre club.',
            );
        }

        const imageUrl = file ? `/uploads/posts/${file.filename}` : undefined;

        return this.postsService.createPost(
            clubId,
            user.userId,
            dto.text,
            imageUrl,
        );
    }

    // PUBLIC: get posts of a club
    @Get('club/:clubId')
    async getClubPosts(@Param('clubId') clubId: string) {
        return this.postsService.getPostsByClub(clubId);
    }

    // PUBLIC: get student feed (posts from clubs the student has joined)
    @Get('feed')
    async getStudentFeed(@Req() req: any) {
        const user = req.user;
        return this.postsService.getStudentFeed(user.userId);
    }

    // PRESIDENT/CLUB: update post
    @Put(':postId')
    @UseGuards(IsPresidentGuard)
    @UseInterceptors(FileInterceptor('image', multerOptions('posts')))
    async update(
        @Param('postId') postId: string,
        @Body() dto: UpdatePostDto,
        @UploadedFile() file: Express.Multer.File,
        @Req() req: any,
    ) {
        const imageUrl = file ? `/uploads/posts/${file.filename}` : undefined;

        return this.postsService.updatePost(
            postId,
            dto.text,
            imageUrl,
            req.user,
        );
    }

    // PRESIDENT/CLUB: delete post
    @Delete(':postId')
    @UseGuards(IsPresidentGuard)
    async remove(@Param('postId') postId: string, @Req() req: any) {
        return this.postsService.deletePost(postId, req.user);
    }

    // STUDENT: like a post
    @Post(':id/like')
    async likePost(@Param('id') postId: string, @Req() req: any) {
        const user = req.user;
        return this.postsService.likePost(postId, user.userId);
    }

    // STUDENT: dislike a post
    @Post(':id/dislike')
    async dislikePost(@Param('id') postId: string, @Req() req: any) {
        const user = req.user;
        return this.postsService.dislikePost(postId, user.userId);
    }

    // STUDENT: comment on a post
    @Post(':id/comment')
    async commentPost(
        @Param('id') postId: string,
        @Body() body: { content: string },
        @Req() req: any,
    ) {
        const user = req.user;
        return this.postsService.commentPost(postId, user.userId, body.content);
    }
}
