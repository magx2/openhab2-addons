package org.openhab.binding.bambulab.internal.rest;

import com.google.gson.Gson;
import java.util.List;

public record UserProfile(
        long uid,
        String name,
        String handle,
        String avatar,
        String bio,
        List<String> links,
        String backgroundUrl,
        List<Long> designIds,
        List<String> designInfos,
        List<Long> pinnedDesignIds,
        int isLikeOpen,
        int isFollowOpen,
        int isFanOpen,
        int isHistoryOpen,
        int isModelSave,
        int isNSFWShown,
        int isUserImprove,
        String defaultLicense,
        List<String> deviceNames,
        int isRecentRatingShown,
        int profilePublished,
        int profileCreated,
        int modelCommented,
        int profileRating,
        int commentReplied,
        int printSuccess,
        int designRemixed,
        int atMentioned,
        int profileModified,
        int modelBoosted,
        int receiveBoost,
        int boostExpire,
        int commentAndRating,
        int modelsRecommendation,
        int followerRecall,
        int postLiked,
        int showUnreadMsgCnt,
        int noticePrintSuccess,
        int noticePrintError,
        int noticeProductUpdate,
        int noticePromotionalContent,
        int noticeFollowerRecall,
        int noticeCommentAndRating,
        int noticePostLiked,
        int emailProfilePublished,
        int emailProfileCreated,
        int emailModelCommented,
        int emailProfileRating,
        int emailCommentReplied,
        int emailDesignRemixed,
        int emailAtMentioned,
        int emailProfileModified,
        int emailModelBoosted,
        int emailReceiveBoost,
        int emailBoostExpire,
        int emailCommentAndRating,
        int emailModelsRecommendation,
        int emailFollowerRecall,
        int emailPostLiked,
        int recommendStatus,
        String country,
        String birthday,
        int gender,
        List<String> likeCategories,
        List<String> blockTags,
        List<String> blockCategories,
        int isSyncOpen,
        int syncMode,
        int contentI18N,
        int visitHistoryStatus,
        int isQuietModeOpen,
        int quietStartHour,
        int quietStartMinute,
        int quietEndtHour,
        int quietEndMinute
) {
}
