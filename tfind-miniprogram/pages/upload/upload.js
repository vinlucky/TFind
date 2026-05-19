var app = getApp();

Page({
  data: {
    name: '',
    latitude: null,
    longitude: null,
    address: '',
    floor: '',
    positions: '',
    cleanScore: 3,
    hasMotherRoom: false,
    hasAccessibility: false,
    isFree: true,
    is24Hours: false,
    isQueuing: false,
    queueTime: '',
    tags: [],
    tagOptions: ['干净', '宽敞', '有纸', '通风好', '有洗手液', '有烘干机', '有镜子', '隐蔽'],
    photoUrl: '',
    baseUrl: '',
    submitting: false
  },

  onLoad: function () {
    this.setData({ baseUrl: app.globalData.baseUrl });
    this.getLocation();
  },

  onShow: function () {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 2 });
    }
  },

  getLocation: function () {
    var that = this;
    if (app.globalData.location) {
      that.setData({
        latitude: app.globalData.location.latitude,
        longitude: app.globalData.location.longitude
      });
      return;
    }
    wx.getLocation({
      type: 'gcj02',
      success: function (res) {
        app.globalData.location = {
          latitude: res.latitude,
          longitude: res.longitude
        };
        that.setData({
          latitude: res.latitude,
          longitude: res.longitude
        });
      }
    });
  },

  getLocationFromWx: function () {
    var that = this;
    wx.chooseLocation({
      success: function (res) {
        that.setData({
          latitude: res.latitude,
          longitude: res.longitude,
          address: res.address || res.name
        });
      }
    });
  },

  onNameInput: function (e) {
    this.setData({ name: e.detail.value });
  },

  onAddressInput: function (e) {
    this.setData({ address: e.detail.value });
  },

  onFloorInput: function (e) {
    this.setData({ floor: e.detail.value });
  },

  onPositionsInput: function (e) {
    this.setData({ positions: e.detail.value });
  },

  onCleanScoreChange: function (e) {
    this.setData({ cleanScore: parseInt(e.detail.value) });
  },

  toggleMotherRoom: function () {
    this.setData({ hasMotherRoom: !this.data.hasMotherRoom });
  },

  toggleAccessibility: function () {
    this.setData({ hasAccessibility: !this.data.hasAccessibility });
  },

  toggleFree: function () {
    this.setData({ isFree: !this.data.isFree });
  },

  toggle24Hours: function () {
    this.setData({ is24Hours: !this.data.is24Hours });
  },

  toggleQueuing: function () {
    this.setData({ isQueuing: !this.data.isQueuing });
  },

  onQueueTimeInput: function (e) {
    this.setData({ queueTime: e.detail.value });
  },

  onTagTap: function (e) {
    var tag = e.currentTarget.dataset.tag;
    var tags = this.data.tags.slice();
    var index = tags.indexOf(tag);
    if (index > -1) {
      tags.splice(index, 1);
    } else {
      tags.push(tag);
    }
    this.setData({ tags: tags });
  },

  chooseImage: function () {
    var that = this;
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: function (res) {
        var tempFilePath = res.tempFilePaths[0];
        that.uploadImage(tempFilePath);
      }
    });
  },

  uploadImage: function (filePath) {
    var that = this;
    var token = app.globalData.token;
    wx.showLoading({ title: '上传中...', mask: true });
    wx.uploadFile({
      url: app.globalData.baseUrl + '/api/toilet/upload',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': token ? 'Bearer ' + token : ''
      },
      success: function (res) {
        wx.hideLoading();
        try {
          var data = JSON.parse(res.data);
          if (data.code === 200) {
            that.setData({ photoUrl: data.data });
            app.showSuccess('图片上传成功');
          } else {
            console.error('上传返回错误:', data);
            app.showError(data.message || '上传失败');
          }
        } catch (e) {
          console.error('解析上传响应失败:', res.data);
          app.showError('上传响应异常');
        }
      },
      fail: function (err) {
        wx.hideLoading();
        console.error('上传文件失败:', err);
        app.showError('上传失败，请检查网络');
      }
    });
  },

  submitForm: function () {
    var that = this;
    if (!app.checkLogin()) return;

    if (!that.data.address) {
      wx.showToast({ title: '请输入地址', icon: 'none' });
      return;
    }
    if (!that.data.latitude || !that.data.longitude) {
      wx.showToast({ title: '请先获取位置信息', icon: 'none' });
      return;
    }

    that.setData({ submitting: true });
    app.showLoading('提交中...');

    var submitData = {
      name: that.data.name,
      latitude: that.data.latitude,
      longitude: that.data.longitude,
      address: that.data.address,
      floor: that.data.floor,
      positions: that.data.positions ? parseInt(that.data.positions) : null,
      cleanScore: that.data.cleanScore,
      hasMotherRoom: that.data.hasMotherRoom,
      hasAccessibility: that.data.hasAccessibility,
      isFree: that.data.isFree,
      is24Hours: that.data.is24Hours,
      isQueuing: that.data.isQueuing,
      queueTime: that.data.queueTime ? parseInt(that.data.queueTime) : null,
      tags: that.data.tags,
      photoUrl: that.data.photoUrl
    };

    app.request({
      url: '/api/toilet',
      method: 'POST',
      data: submitData,
      success: function (res) {
        app.hideLoading();
        that.setData({ submitting: false });
        if (res.data && res.data.code === 200) {
          var status = res.data.data && res.data.data.status;
          var message = res.data.message || '';
          if (status === 'approved') {
            app.showSuccess(message || '审核通过，厕所已发布');
          } else {
            app.showSuccess(message || '已提交，等待管理员审核');
          }
          that.resetForm();
        } else {
          var errMsg = (res.data && res.data.message) ? res.data.message : '提交失败';
          console.error('提交失败:', res.data);
          wx.showModal({
            title: '审核不通过',
            content: errMsg,
            showCancel: false
          });
        }
      },
      fail: function () {
        app.hideLoading();
        that.setData({ submitting: false });
        app.showError('网络错误');
      }
    });
  },

  resetForm: function () {
    this.setData({
      name: '',
      address: '',
      floor: '',
      positions: '',
      cleanScore: 3,
      hasMotherRoom: false,
      hasAccessibility: false,
      isFree: true,
      is24Hours: false,
      isQueuing: false,
      queueTime: '',
      tags: [],
      photoUrl: ''
    });
  }
});
