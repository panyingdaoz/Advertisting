package com.kingbird.advertisting.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kingbird.advertisting.litepal.PlayList;
import com.kingbird.advertisting.manager.ThreadManager;
import com.kingbird.advertisting.utils.Const;
import com.kingbird.advertisting.utils.Plog;
import com.kingbird.advertisting.utils.SpUtil;
import com.youth.banner.BannerConfig;
import com.youth.banner.BannerScroller;
import com.youth.banner.WeakHandler;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoaderInterface;
import com.youth.banner.view.BannerViewPager;

import org.litepal.LitePal;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.kingbird.advertisting.base.Base.dataQuery;
import static com.kingbird.advertisting.base.Base.getQuery;
import static com.kingbird.advertisting.base.Base.intentActivity;
import static com.kingbird.advertisting.base.Base.setCount;
import static com.kingbird.advertisting.base.Base.showReport;
import static com.kingbird.advertisting.utils.Config.FILE_SAVE_URL;

//import com.youth.banner.listener.OnBannerClickListener;

/**
 * 类具体作用
 *
 * @author Panyingdao
 * @date 2019/1/23/023.
 */
public class Banner2 extends FrameLayout implements ViewPager.OnPageChangeListener {
    public String tag = "banner";
    private int mIndicatorMargin = BannerConfig.PADDING_SIZE;
    private int mIndicatorWidth;
    private int mIndicatorHeight;
    private int indicatorSize;
    private int bannerBackgroundImage;
    private int bannerStyle = BannerConfig.CIRCLE_INDICATOR;
    private int delayTime = BannerConfig.TIME;
    private int scrollTime = BannerConfig.DURATION;
    private boolean isAutoPlay = BannerConfig.IS_AUTO_PLAY;
    private boolean isScroll = BannerConfig.IS_SCROLL;
    private int mIndicatorSelectedResId = com.youth.banner.R.drawable.gray_radius;
    private int mIndicatorUnselectedResId = com.youth.banner.R.drawable.white_radius;
    private int mLayoutResId = com.youth.banner.R.layout.banner;
    private int titleHeight;
    private int titleBackground;
    private int titleTextColor;
    private int titleTextSize;
    private int count = 0;
    private int currentItem;
    private int gravity = -1;
    private int lastPosition = 1;
    private int scaleType = 1;
    //    private List<String> titles;
    private List imageUrls;
    private List<View> imageViews;
    private List<ImageView> indicatorImages;
    private Context context;
    private BannerViewPager viewPager;
    private TextView bannerTitle, numIndicatorInside, numIndicator;
    private LinearLayout indicator, indicatorInside, titleView;
    private ImageView bannerDefaultImage;
    private ImageLoaderInterface imageLoader;
    private BannerPagerAdapter adapter;
    private ViewPager.OnPageChangeListener mOnPageChangeListener;
    //    private OnBannerClickListener bannerListener;
    private OnBannerListener listener;

    private WeakHandler handler = new WeakHandler();

    public Banner2(Context context) {
        this(context, null);
    }

    public Banner2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Banner2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
//        titles = new ArrayList<>();
        imageUrls = new ArrayList<>();
        imageViews = new ArrayList<>();
        indicatorImages = new ArrayList<>();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        indicatorSize = dm.widthPixels / 80;
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        imageViews.clear();
        handleTypedArray(context, attrs);
        View view = LayoutInflater.from(context).inflate(mLayoutResId, this, true);
        bannerDefaultImage = view.findViewById(com.youth.banner.R.id.bannerDefaultImage);
        viewPager = view.findViewById(com.youth.banner.R.id.bannerViewPager);
        titleView = view.findViewById(com.youth.banner.R.id.titleView);
        indicator = view.findViewById(com.youth.banner.R.id.circleIndicator);
        indicatorInside = view.findViewById(com.youth.banner.R.id.indicatorInside);
        bannerTitle = view.findViewById(com.youth.banner.R.id.bannerTitle);
        numIndicator = view.findViewById(com.youth.banner.R.id.numIndicator);
        numIndicatorInside = view.findViewById(com.youth.banner.R.id.numIndicatorInside);
        bannerDefaultImage.setImageResource(bannerBackgroundImage);
        initViewPagerScroll();
    }

    private void handleTypedArray(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
//        int[] banner = {0x7f030036, 0x7f030037, 0x7f030069, 0x7f030092, 0x7f030094, 0x7f030095, 0x7f030096, 0x7f030097, 0x7f030098, 0x7f03009c, 0x7f030101, 0x7f03013e, 0x7f03013f, 0x7f030140, 0x7f030141};
//        TypedArray typedArray = context.obtainStyledAttributes(attrs, banner);
        @SuppressLint("CustomViewStyleable") TypedArray typedArray = context.obtainStyledAttributes(attrs, com.youth.banner.R.styleable.Banner);
        mIndicatorWidth = typedArray.getDimensionPixelSize(com.youth.banner.R.styleable.Banner_indicator_width, indicatorSize);
        mIndicatorHeight = typedArray.getDimensionPixelSize(com.youth.banner.R.styleable.Banner_indicator_height, indicatorSize);
        mIndicatorMargin = typedArray.getDimensionPixelSize(com.youth.banner.R.styleable.Banner_indicator_margin, BannerConfig.PADDING_SIZE);
        mIndicatorSelectedResId = typedArray.getResourceId(com.youth.banner.R.styleable.Banner_indicator_drawable_selected, com.youth.banner.R.drawable.gray_radius);
        mIndicatorUnselectedResId = typedArray.getResourceId(com.youth.banner.R.styleable.Banner_indicator_drawable_unselected, com.youth.banner.R.drawable.white_radius);
        scaleType = typedArray.getInt(com.youth.banner.R.styleable.Banner_image_scale_type, scaleType);
        delayTime = typedArray.getInt(com.youth.banner.R.styleable.Banner_delay_time, BannerConfig.TIME);
        scrollTime = typedArray.getInt(com.youth.banner.R.styleable.Banner_scroll_time, BannerConfig.DURATION);
        isAutoPlay = typedArray.getBoolean(com.youth.banner.R.styleable.Banner_is_auto_play, BannerConfig.IS_AUTO_PLAY);
        titleBackground = typedArray.getColor(com.youth.banner.R.styleable.Banner_title_background, BannerConfig.TITLE_BACKGROUND);
        titleHeight = typedArray.getDimensionPixelSize(com.youth.banner.R.styleable.Banner_title_height, BannerConfig.TITLE_HEIGHT);
        titleTextColor = typedArray.getColor(com.youth.banner.R.styleable.Banner_title_textcolor, BannerConfig.TITLE_TEXT_COLOR);
        titleTextSize = typedArray.getDimensionPixelSize(com.youth.banner.R.styleable.Banner_title_textsize, BannerConfig.TITLE_TEXT_SIZE);
        mLayoutResId = typedArray.getResourceId(com.youth.banner.R.styleable.Banner_banner_layout, mLayoutResId);
        bannerBackgroundImage = typedArray.getResourceId(com.youth.banner.R.styleable.Banner_banner_default_image, com.youth.banner.R.drawable.no_banner);
        typedArray.recycle();
    }

    private void initViewPagerScroll() {
        try {
            Field mField = ViewPager.class.getDeclaredField("mScroller");
            mField.setAccessible(true);
            BannerScroller mScroller = new BannerScroller(viewPager.getContext());
            mScroller.setDuration(scrollTime);
            mField.set(viewPager, mScroller);
        } catch (Exception e) {
            Log.e(tag, e.getMessage());
        }
    }

    public void setImageLoader(ImageLoaderInterface imageLoader) {
        this.imageLoader = imageLoader;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    public void setIndicatorGravity(int type) {
        switch (type) {
            case BannerConfig.LEFT:
//                this.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
                this.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                break;
            case BannerConfig.CENTER:
                this.gravity = Gravity.CENTER;
                break;
            case BannerConfig.RIGHT:
//                this.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
                this.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
                break;
            default:
                break;
        }
//        return this;
    }

//    public Banner2 setBannerStyle(int bannerStyle) {
    public void setBannerStyle(int bannerStyle) {
        this.bannerStyle = bannerStyle;
//        return this;
    }

    public void setViewPagerIsScroll(boolean isScroll) {
        this.isScroll = isScroll;
    }

    public void setImages(List<?> imageUrls) {
        this.imageUrls = imageUrls;
        this.count = imageUrls.size();
    }


    public void update(List<?> imageUrls) {
        this.imageUrls.clear();
        this.imageViews.clear();
        this.indicatorImages.clear();
//        this.imageUrls.addAll(imageUrls);
        this.imageUrls.addAll(imageUrls);
        this.count = this.imageUrls.size();
        start2();
    }

    public int getCurrentItem() {
        return viewPager.getCurrentItem();
    }

//    public Banner2 start() {
    public void start() {
        setBannerStyleUi();
        setImageList(imageUrls);
        setData();
//        return this;
    }

    public void start2() {
        setBannerStyleUi();
        setImageList(imageUrls);
        setData2();
    }

    private void setTitleStyleUi() {
//        if (titles.size() != imageUrls.size()) {
//            throw new RuntimeException("[Banner] --> The number of titles and images is different");
//        }
        if (titleBackground != -1) {
            titleView.setBackgroundColor(titleBackground);
        }
        if (titleHeight != -1) {
            titleView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, titleHeight));
        }
        if (titleTextColor != -1) {
            bannerTitle.setTextColor(titleTextColor);
        }
        if (titleTextSize != -1) {
            bannerTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);
        }
//        if (titles != null && titles.size() > 0) {
//            bannerTitle.setText(titles.get(0));
//            bannerTitle.setVisibility(View.VISIBLE);
//            titleView.setVisibility(View.VISIBLE);
//        }
    }

    private void setBannerStyleUi() {
        int visibility = count > 1 ? View.VISIBLE : View.GONE;
        switch (bannerStyle) {
            case BannerConfig.CIRCLE_INDICATOR:
                indicator.setVisibility(visibility);
                break;
            case BannerConfig.NUM_INDICATOR:
                numIndicator.setVisibility(visibility);
                break;
            case BannerConfig.NUM_INDICATOR_TITLE:
                numIndicatorInside.setVisibility(visibility);
                setTitleStyleUi();
                break;
            case BannerConfig.CIRCLE_INDICATOR_TITLE:
                indicator.setVisibility(visibility);
                setTitleStyleUi();
                break;
            case BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE:
                indicatorInside.setVisibility(visibility);
                setTitleStyleUi();
                break;
            default:
        }
    }

    @SuppressLint("SetTextI18n")
    private void initImages() {
        imageViews.clear();
        if (bannerStyle == BannerConfig.CIRCLE_INDICATOR ||
                bannerStyle == BannerConfig.CIRCLE_INDICATOR_TITLE ||
                bannerStyle == BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE) {
            createIndicator();
        } else if (bannerStyle == BannerConfig.NUM_INDICATOR_TITLE) {
            numIndicatorInside.setText("1/" + count);
        } else if (bannerStyle == BannerConfig.NUM_INDICATOR) {
            numIndicator.setText("1/" + count);
        }
    }

    private void setImageList(List<?> imagesUrl) {
        if (imagesUrl == null || imagesUrl.size() <= 0) {
            bannerDefaultImage.setVisibility(VISIBLE);
            Log.e(tag, "The image data set is empty.");
            return;
        }
        bannerDefaultImage.setVisibility(GONE);
        initImages();
        for (int i = 0; i <= count + 1; i++) {
            View imageView = null;
            if (imageLoader != null) {
                imageView = imageLoader.createImageView(context);
            }
            if (imageView == null) {
                imageView = new ImageView(context);
            }
            setScaleType(imageView);
            Object url;
            if (i == 0) {
                url = imagesUrl.get(count - 1);
            } else if (i == count + 1) {
                url = imagesUrl.get(0);
            } else {
                url = imagesUrl.get(i - 1);
            }
            imageViews.add(imageView);
            if (imageLoader != null) {
                imageLoader.displayImage(context, url, imageView);
            } else {
                Log.e(tag, "Please set images loader.");
            }
        }
    }

    private void setScaleType(View imageView) {
        if (imageView instanceof ImageView) {
            ImageView view = ((ImageView) imageView);
            switch (scaleType) {
                case 0:
                    view.setScaleType(ImageView.ScaleType.CENTER);
                    break;
                case 1:
                    view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    break;
                case 2:
                    view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    break;
                case 3:
                    view.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    break;
                case 4:
                    view.setScaleType(ImageView.ScaleType.FIT_END);
                    break;
                case 5:
                    view.setScaleType(ImageView.ScaleType.FIT_START);
                    break;
                case 6:
                    view.setScaleType(ImageView.ScaleType.FIT_XY);
                    break;
                case 7:
                    view.setScaleType(ImageView.ScaleType.MATRIX);
                    break;
                default:
            }

        }
    }

    private void createIndicator() {
        indicatorImages.clear();
        indicator.removeAllViews();
        indicatorInside.removeAllViews();
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mIndicatorWidth, mIndicatorHeight);
            params.leftMargin = mIndicatorMargin;
            params.rightMargin = mIndicatorMargin;
            if (i == 0) {
                imageView.setImageResource(mIndicatorSelectedResId);
            } else {
                imageView.setImageResource(mIndicatorUnselectedResId);
            }
            indicatorImages.add(imageView);
            if (bannerStyle == BannerConfig.CIRCLE_INDICATOR ||
                    bannerStyle == BannerConfig.CIRCLE_INDICATOR_TITLE) {
                indicator.addView(imageView, params);
            } else if (bannerStyle == BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE) {
                indicatorInside.addView(imageView, params);
            }
        }
    }

    private void setData() {
        currentItem = 1;
        if (adapter == null) {
            adapter = new BannerPagerAdapter();
            viewPager.addOnPageChangeListener(this);
        }
        viewPager.setAdapter(adapter);
        viewPager.setFocusable(true);
        viewPager.setCurrentItem(1);
        if (gravity != -1) {
            indicator.setGravity(gravity);
        }
        if (isScroll && count > 1) {
            viewPager.setScrollable(true);
        } else {
            viewPager.setScrollable(false);
        }
        if (isAutoPlay) {
            startAutoPlay();
        }
    }

    private void setData2() {
        int item = viewPager.getCurrentItem();
        currentItem = item;
        if (adapter == null) {
            adapter = new BannerPagerAdapter();
            viewPager.addOnPageChangeListener(this);
        }
        viewPager.setAdapter(adapter);
        viewPager.setFocusable(true);
        viewPager.setCurrentItem(item);
        if (gravity != -1) {
            indicator.setGravity(gravity);
        }
        if (isScroll && count > 1) {
            viewPager.setScrollable(true);
        } else {
            viewPager.setScrollable(false);
        }
        if (isAutoPlay) {
            startAutoPlay();
        }
    }

    public void startAutoPlay() {
        handler.removeCallbacks(task);
        handler.postDelayed(task, delayTime);
    }

    public void stopAutoPlay() {
        handler.removeCallbacks(task);
    }

    private final Runnable task = new Runnable() {
        @Override
        public void run() {
            Plog.e("副屏列表大小", count);
            if (count > 1 && isAutoPlay) {
                currentItem = currentItem % (count + 1) + 1;
                Plog.e("每次切换的下标", currentItem);
                //列表巡查
                showCompare();

                if (currentItem == 1) {
                    viewPager.setCurrentItem(currentItem, false);
                    handler.post(task);
                } else {
                    viewPager.setCurrentItem(currentItem);
                    handler.postDelayed(task, delayTime);
                }
            } else if (count == 1) {
                Plog.e("只有一个副屏广告", currentItem);
                viewPager.setCurrentItem(currentItem);
                handler.postDelayed(task, delayTime);
                showCompare();
            }
        }
    };

    public void showCompare() {
        int queryId;
//        Plog.e("viewPage数组", imageUrls);
        int imageUrlsSize = imageUrls.size();
        if (currentItem > imageUrlsSize) {
            queryId = 0;
        } else if (currentItem == imageUrlsSize) {
            queryId = currentItem - 1;
        } else {
            queryId = currentItem - 1;
        }
        for (int i = 0; i < imageUrlsSize; i++) {
            if (queryId == i) {
                String fileUrl = imageUrls.get(i).toString();
                String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                Plog.e("正在播放的文件名", fileName);
                qery = getQuery("playId", "fileName", fileName);
                for (PlayList playList : qery) {
                    String playId = Integer.toString(playList.getPlayId());
//                    Plog.e("查询到的播放ID", playId);
                    localShowPlay(playId);
                }
            }
        }
    }

    public int getBannerSize() {
        return imageUrls.size();
    }

    public void setBannerPlayItem(String fileName) {
        int imageUrlsSize = imageUrls.size();
        String fileUrl = FILE_SAVE_URL + fileName;
        Plog.e("要播放的文件名", fileName);
        for (int i = 0; i < imageUrlsSize; i++) {
            String fileUrls = imageUrls.get(i).toString();
            Plog.e("查询的文件路径", fileUrls);
            if (fileUrl.equals(fileUrls)) {
                if (i == 0) {
                    currentItem = imageUrlsSize + 1;
                } else {
                    currentItem = i + 1;
                }
                Plog.e("跳转的item和时间", currentItem + "\n" + delayTime);
                viewPager.setCurrentItem(currentItem);
                handler.postDelayed(task, delayTime);
            }
        }
    }

    public void deleteBannerPlayItem(String fileName) {
        List<PlayList> queryPlayId = getQuery("playId", "fileName", fileName);
        for (PlayList queryPlayIds : queryPlayId) {
            deleteShow(Integer.toString(queryPlayIds.getPlayId()));
        }
    }

    private List<PlayList> qery;

    /**
     * 综合播放处理
     */
    private void localShowPlay(String playListId) {
        qery = dataQuery("condition", playListId);
        for (PlayList conditions : qery) {
            String playType = conditions.getCondition();
//            Plog.e("播放类型", playType);
            switch (playType) {
                case "00":
                    qery = dataQuery("duration", playListId);
                    for (PlayList duration : qery) {
                        int queryDuration = duration.getDuration();
//                        Plog.e("查询设定播放次数", queryDuration);
                        int count = getCurrentCount(playListId);
//                        Plog.e("已播放次数", count);
                        if (count >= queryDuration) {
                            deleteShow(playListId);
                        } else if (count < duration.getDuration()) {
                            int playCount = count + 1;
                            String deviceId = SpUtil.readString(Const.DEVICE_ID);
                            String rCode = SpUtil.readString(Const.RCODE);
                            setCount(playListId, playCount);
                            showReport(deviceId, rCode, playListId, playCount);
                        }
                    }
                    break;
                case "01":
                    timePlay(playListId);
                    break;
                case "90":
                    Plog.e("删除无效节目清单");
                    deleteShow(playListId);
                    break;
                case "80":
                    Plog.e("删除无效节目清单");
                    deleteShow(playListId);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 时间播放处理
     */
    private void timePlay(final String playListId) {
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                String startTime = null, endTime = null;
                Date startDate, endDate;
                Date currentDate = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

                List<PlayList> newsList = LitePal.findAll(PlayList.class, Integer.parseInt(playListId));
                for (PlayList newsLists : newsList) {
                    Plog.e("播放开始时间", newsLists.getStartTime());
                }

                qery = dataQuery("startTime", playListId);
                for (PlayList start : qery) {
                    startTime = start.getStartTime();
                }
                qery = dataQuery("endTime", playListId);
                for (PlayList end : qery) {
                    endTime = end.getEndTime();
                }

                try {
                    startDate = format.parse(startTime);
                    endDate = format.parse(endTime);
//                    Plog.e("起始时间", startTime);
//                    Plog.e("现在时间", format.format(currentDate));
//                    Plog.e("终止时间", endTime);
                    if (startDate.before(currentDate) && endDate.after(currentDate)) {
                        Plog.e("可以播放");
                        int count = getCurrentCount(playListId);
                        int playCount = count + 1;
                        String deviceId = SpUtil.readString(Const.DEVICE_ID);
                        String rCode = SpUtil.readString(Const.RCODE);
                        setCount(playListId, playCount);
                        showReport(deviceId, rCode, playListId, playCount);
                    } else if (currentDate.before(startDate)) {
                        Plog.e("时间未到，播放下一个");
                        currentItem++;
                        if (currentItem > (imageUrls.size() + 1)) {
                            currentItem = 2;
                        }
                    } else {
                        deleteShow(playListId);
                    }
                } catch (ParseException e) {
                    Plog.e("异常", e.toString());
                    e.printStackTrace();
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 获取当前节目播放次数
     *
     * @param playListId 节目ID
     */
    private int getCurrentCount(String playListId) {
        int count = 0;
        qery = dataQuery("count", playListId);
        for (PlayList qerys : qery) {
            count = qerys.getCount();
        }
        return count;
    }

    /**
     * 删除清单
     */
    private void deleteShow(String playListId) {
        List<PlayList> fileName = dataQuery("fileName", playListId);
        for (PlayList fileNames : fileName) {
            List<PlayList> showIdQuery = dataQuery("showId", playListId);
            for (PlayList showIdQuerys : showIdQuery) {
                String showId = Integer.toString(showIdQuerys.getShowId());
                String deleteFileName = fileNames.getFileName();
                int isDelete = LitePal.deleteAll(PlayList.class, "playId = ?", playListId);
                Plog.e("分屏广告清单删除结果", isDelete);
                if (isDelete > 0) {
//                    for (int i = 0; i < imageUrls.size(); i++) {
                    for (int i = imageUrls.size() - 1; i >= 0; i--) {
                        String fileUrl = FILE_SAVE_URL + deleteFileName;
                        String showFile = imageUrls.get(i).toString();
                        if (showFile.equals(fileUrl)) {
                            imageUrls.remove(i);
                            String server = SpUtil.readString(Const.CONTROL_TYPE);
                            Base.deleteFile(deleteFileName, server);
                            SpUtil.writeString(Const.SHOW_ID2, showId);
                            intentActivity("17");
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isAutoPlay) {
            int action = ev.getAction();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL
                    || action == MotionEvent.ACTION_OUTSIDE) {
                startAutoPlay();
            } else if (action == MotionEvent.ACTION_DOWN) {
                stopAutoPlay();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 返回真实的位置
     *
     * @param position 位置
     * @return 下标从0开始
     */
    public int toRealPosition(int position) {
        int realPosition = (position - 1) % count;
        if (realPosition < 0) {
            realPosition += count;
        }
        return realPosition;
    }

    class BannerPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return imageViews.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            container.addView(imageViews.get(position));
            View view = imageViews.get(position);
//            if (bannerListener != null) {
//                view.setOnClickListener(new OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Log.e(tag, "你正在使用旧版点击事件接口，下标是从1开始，" +
//                                "为了体验请更换为setOnBannerListener，下标从0开始计算");
//                        bannerListener.OnBannerClick(position);
//                    }
//                });
//            }
            if (listener != null) {
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.OnBannerClick(toRealPosition(position));
                    }
                });
            }
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrollStateChanged(state);
        }
        switch (state) {
            //No operation
            case 0:
                if (currentItem == 0) {
                    viewPager.setCurrentItem(count, false);
                } else if (currentItem == count + 1) {
                    viewPager.setCurrentItem(1, false);
                }
                break;
            //start Sliding
            case 1:
                if (currentItem == count + 1) {
                    viewPager.setCurrentItem(1, false);
                } else if (currentItem == 0) {
                    viewPager.setCurrentItem(count, false);
                }
                break;
            //end Sliding
            case 2:
                break;
            default:
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrolled(toRealPosition(position), positionOffset, positionOffsetPixels);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onPageSelected(int position) {
        currentItem = position;
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageSelected(toRealPosition(position));
        }
        if (bannerStyle == BannerConfig.CIRCLE_INDICATOR ||
                bannerStyle == BannerConfig.CIRCLE_INDICATOR_TITLE ||
                bannerStyle == BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE) {
            indicatorImages.get((lastPosition - 1 + count) % count).setImageResource(mIndicatorUnselectedResId);
            indicatorImages.get((position - 1 + count) % count).setImageResource(mIndicatorSelectedResId);
            lastPosition = position;
        }
        if (position == 0) {
            position = count;
        }
        if (position > count) {
            position = 1;
        }
        switch (bannerStyle) {
            case BannerConfig.CIRCLE_INDICATOR:
                break;
            case BannerConfig.NUM_INDICATOR:
                numIndicator.setText(position + "/" + count);
                break;
            case BannerConfig.NUM_INDICATOR_TITLE:
                numIndicatorInside.setText(position + "/" + count);
//                bannerTitle.setText(titles.get(position - 1));
                break;
            case BannerConfig.CIRCLE_INDICATOR_TITLE:
//                bannerTitle.setText(titles.get(position - 1));
                break;
            case BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE:
//                bannerTitle.setText(titles.get(position - 1));
                break;
            default:
        }

    }
//
//    @Deprecated
//    public Banner2 setOnBannerClickListener(OnBannerClickListener listener) {
//        this.bannerListener = listener;
//        return this;
//    }

    @Deprecated
    public Banner2 setOnBannerClickListener(OnBannerListener listener) {
        this.listener = listener;
        return this;
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        mOnPageChangeListener = onPageChangeListener;
    }

}
