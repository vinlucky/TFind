var app = getApp();

Page({
  data: {
    toilet: null,
    reviews: [],
    reviewUpdates: [],
    showReviewForm: false,
    showUpdateForm: false,
    showReportForm: false,
    reviewContent: '',
    reviewScore: 3,
    reviewTags: [],
    reviewTagOptions: ['干净', '脏乱', '有纸', '无纸', '宽敞', '拥挤', '有味', '通风好'],
    updateScore: 3,
    updateTags: [],
    updateTagOptions: ['干净', '脏乱', '有纸', '无纸', '宽敞', '拥挤', '排队中', '无需排队'],
    updateIsFree: true,
    updateHasMotherRoom: false,
    updateHasAccessibility: false,
    updateIs24Hours: false,
    updateIsQueuing: false,
    updateQueueTime: '',
    updateContent: '',
    reportType: '',
    reportContent: '',
    reportTypes: ['信息错误', '已关闭', '不存在', '其他'],
    distanceText: '',
    isAdmin: false,
    currentUserId: ''
  },

  onLoad: function (options) {
    if (options.id) {
      this.loadToiletById(options.id);
    }
    var userInfo = app.globalData.userInfo;
    if (userInfo) {
      this.setData({
        currentUserId: userInfo.userId,
        isAdmin: userInfo.role === 'ADMIN' || userInfo.role === 'admin'
      });
    }
  },

  loadToiletById: function (id) {
    var that = this;
    app.showLoading();
    app.request({
      url: '/api/toilet/' + id,
      method: 'GET',
      success: function (res) {
        app.hideLoading();
        if (res.data && res.data.code === 200) {
          var toilet = res.data.data;
          if (app.globalData.location && toilet.latitude && toilet.longitude) {
            var dist = app.calculateDistance(
              app.globalData.location.latitude,
              app.globalData.location.longitude,
              toilet.latitude, toilet.longitude
            );
            if (dist < 1) {
              toilet.distanceText = (dist * 1000).toFixed(0) + 'm';
            } else {
              toilet.distanceText = dist.toFixed(1) + 'km';
            }
          }
          that.setData({ toilet: toilet });
          that.loadReviews(id);
          that.loadReviewUpdates(id);
        } else {
          app.showError('加载失败');
        }
      },
      fail: function () {
        app.hideLoading();
        app.showError('网络错误');
      }
    });
  },

  loadReviews: function (toiletId) {
    var that = this;
    app.request({
      url: '/api/review/toilet/' + toiletId,
      method: 'GET',
      success: function (res) {
        if (res.data && res.data.code === 200) {
          that.setData({ reviews: res.data.data || [] });
        }
      }
    });
  },

  loadReviewUpdates: function (toiletId) {
    var that = this;
    app.request({
      url: '/api/review-update/toilet/' + toiletId,
      method: 'GET',
      success: function (res) {
        if (res.data && res.data.code === 200) {
          that.setData({ reviewUpdates: res.data.data || [] });
        }
      }
    });
  },

  toggleReviewForm: function () {
    if (!app.checkLogin()) return;
    this.setData({ showReviewForm: !this.data.showReviewForm, showUpdateForm: false, showReportForm: false });
  },

  toggleUpdateForm: function () {
    if (!app.checkLogin()) return;
    this.setData({ showUpdateForm: !this.data.showUpdateForm, showReviewForm: false, showReportForm: false });
  },

  toggleReportForm: function () {
    if (!app.checkLogin()) return;
    this.setData({ showReportForm: !this.data.showReportForm, showReviewForm: false, showUpdateForm: false });
  },

  onReviewContentInput: function (e) {
    this.setData({ reviewContent: e.detail.value });
  },

  onReviewScoreChange: function (e) {
    this.setData({ reviewScore: parseInt(e.detail.value) });
  },

  onReviewTagTap: function (e) {
    var tag = e.currentTarget.dataset.tag;
    var tags = this.data.reviewTags.slice();
    var index = tags.indexOf(tag);
    if (index > -1) {
      tags.splice(index, 1);
    } else {
      tags.push(tag);
    }
    this.setData({ reviewTags: tags });
  },

  submitReview: function () {
    var that = this;
    if (!that.data.reviewContent) {
      wx.showToast({ title: '请输入评论内容', icon: 'none' });
      return;
    }
    app.showLoading('提交中...');
    app.request({
      url: '/api/review',
      method: 'POST',
      data: {
        toiletId: that.data.toilet.id,
        content: that.data.reviewContent,
        score: that.data.reviewScore,
        tags: that.data.reviewTags,
        userName: app.globalData.userInfo ? app.globalData.userInfo.userId : ''
      },
      success: function (res) {
        app.hideLoading();
        if (res.data && res.data.code === 200) {
          app.showSuccess('评论成功');
          that.setData({
            showReviewForm: false,
            reviewContent: '',
            reviewScore: 3,
            reviewTags: []
          });
          that.loadReviews(that.data.toilet.id);
        } else {
          console.error('评论失败:', res.data);
          app.showError('评论失败');
        }
      },
      fail: function () {
        app.hideLoading();
        app.showError('网络错误');
      }
    });
  },

  deleteReview: function (e) {
    var that = this;
    var reviewId = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条评论吗？',
      success: function (res) {
        if (res.confirm) {
          app.showLoading('删除中...');
          app.request({
            url: '/api/review/' + reviewId,
            method: 'DELETE',
            success: function (res2) {
              app.hideLoading();
              if (res2.data && res2.data.code === 200) {
                app.showSuccess('删除成功');
                that.loadReviews(that.data.toilet.id);
              } else {
                console.error('删除失败:', res2.data);
                app.showError('删除失败');
              }
            },
            fail: function () {
              app.hideLoading();
              app.showError('网络错误');
            }
          });
        }
      }
    });
  },

  onUpdateScoreChange: function (e) {
    this.setData({ updateScore: parseInt(e.detail.value) });
  },

  onUpdateTagTap: function (e) {
    var tag = e.currentTarget.dataset.tag;
    var tags = this.data.updateTags.slice();
    var index = tags.indexOf(tag);
    if (index > -1) {
      tags.splice(index, 1);
    } else {
      tags.push(tag);
    }
    this.setData({ updateTags: tags });
  },

  toggleUpdateIsFree: function () {
    this.setData({ updateIsFree: !this.data.updateIsFree });
  },

  toggleUpdateHasMotherRoom: function () {
    this.setData({ updateHasMotherRoom: !this.data.updateHasMotherRoom });
  },

  toggleUpdateHasAccessibility: function () {
    this.setData({ updateHasAccessibility: !this.data.updateHasAccessibility });
  },

  toggleUpdateIs24Hours: function () {
    this.setData({ updateIs24Hours: !this.data.updateIs24Hours });
  },

  toggleUpdateIsQueuing: function () {
    this.setData({ updateIsQueuing: !this.data.updateIsQueuing });
  },

  onUpdateQueueTimeInput: function (e) {
    this.setData({ updateQueueTime: e.detail.value });
  },

  onUpdateContentInput: function (e) {
    this.setData({ updateContent: e.detail.value });
  },

  submitUpdate: function () {
    var that = this;
    app.showLoading('提交中...');
    app.request({
      url: '/api/review-update',
      method: 'POST',
      data: {
        toiletId: that.data.toilet.id,
        score: that.data.updateScore,
        tags: that.data.updateTags,
        isFree: that.data.updateIsFree,
        hasMotherRoom: that.data.updateHasMotherRoom,
        hasAccessibility: that.data.updateHasAccessibility,
        is24Hours: that.data.updateIs24Hours,
        isQueuing: that.data.updateIsQueuing,
        queueTime: that.data.updateQueueTime ? parseInt(that.data.updateQueueTime) : null,
        content: that.data.updateContent,
        userName: app.globalData.userInfo ? app.globalData.userInfo.userId : ''
      },
      success: function (res) {
        app.hideLoading();
        if (res.data && res.data.code === 200) {
          app.showSuccess('更新提交成功');
          that.setData({
            showUpdateForm: false,
            updateScore: 3,
            updateTags: [],
            updateIsFree: true,
            updateHasMotherRoom: false,
            updateHasAccessibility: false,
            updateIs24Hours: false,
            updateIsQueuing: false,
            updateQueueTime: '',
            updateContent: ''
          });
          that.loadReviewUpdates(that.data.toilet.id);
        } else {
          console.error('提交失败:', res.data);
          app.showError('提交失败');
        }
      },
      fail: function () {
        app.hideLoading();
        app.showError('网络错误');
      }
    });
  },

  onReportTypeChange: function (e) {
    this.setData({ reportType: this.data.reportTypes[e.detail.value] });
  },

  onReportContentInput: function (e) {
    this.setData({ reportContent: e.detail.value });
  },

  submitReport: function () {
    var that = this;
    if (!that.data.reportType) {
      wx.showToast({ title: '请选择举报类型', icon: 'none' });
      return;
    }
    app.showLoading('提交中...');
    app.request({
      url: '/api/report',
      method: 'POST',
      data: {
        toiletId: that.data.toilet.id,
        toiletName: that.data.toilet.name,
        reportType: that.data.reportType,
        content: that.data.reportContent
      },
      success: function (res) {
        app.hideLoading();
        if (res.data && res.data.code === 200) {
          app.showSuccess('举报成功');
          that.setData({
            showReportForm: false,
            reportType: '',
            reportContent: ''
          });
        } else {
          console.error('举报失败:', res.data);
          app.showError('举报失败');
        }
      },
      fail: function () {
        app.hideLoading();
        app.showError('网络错误');
      }
    });
  },

  openMap: function () {
    var toilet = this.data.toilet;
    if (!toilet) return;
    wx.openLocation({
      latitude: toilet.latitude,
      longitude: toilet.longitude,
      name: toilet.name,
      address: toilet.address || ''
    });
  }
});
