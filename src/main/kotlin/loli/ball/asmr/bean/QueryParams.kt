@file:Suppress("unused", "EnumEntryName")

package loli.ball.asmr.bean

enum class QuerySort {
    asc,                //顺序
    desc                //倒序
}

enum class WorksOrder {
    release,            //发布日期
    create_date,        //收录时间 asmr.one 专用
    created_at,         //收录时间 其他kikoeru后端
    rating,             //评价
    dl_count,           //下载量
    price,              //价格
    rate_average_2dp,   //评价
    review_count,       //评论数量
    id,                 //RJ号
    nsfw,               //全年龄
    random              //随机
}

enum class ReviewOrder {
    updated_at,         //标记时间
    userRating,         //评价
    release,            //发布时间
    review_count,       //评论数量
    dl_count,           //售出数量
    nsfw                //年龄分级 (asc: sfw->nsfw)
}

enum class ListenState {
    marked,             //想听
    listening,          //在听
    listened,           //听过
    replay,             //重听
    postponed           //搁置
}
