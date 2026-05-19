var app = getApp();

Page({
  data: {
    isAdmin: false,
    activeTab: 'pending',
    tabs: [
      { key: 'pending', name: '待审核' },
      { key: 'userManage', name: '用户管理' },
      { key: 'deletedUsers', name: '已删除用户' },
      { key: 'deletedToilets', name: '已删除厕所' }
    ],
    pendingToilets: [],
    users: [],
    deletedUsers: [],
    deletedToilets: [],
    addAdminUserId: '',
    deleteUserUserId: ''
  },

  onLoad: function () {
    this.checkAdmin();
  },

  checkAdmin: function () {
    var userInfo = app.globalData.userInfo;
    if (!userInfo) {
      wx.showModal({
        title: '无权限',
        content: '请先登录',
        showCancel: false,
        success: function () {
          wx.navigateBack();
        }
      });
      return;
    }
    if (userInfo.role !== 'ADMIN' && userInfo.role !== 'admin') {
      wx.showModal({
        title: '无权限',
        content: '仅管理员可访问此页面',
        showCancel: false,
        success: function () {
          wx.navigateBack();
        }
      });
      return;
    }
    this.setData({ isAdmin: true });
    this.loadData();
  },

  onTabChange: function (e) {
    var key = e.currentTarget.dataset.key;
    this.setData({ activeTab: key });
    this.loadData();
  },

  loadData: function () {
    var activeTab = this.data.activeTab;
    if (activeTab === 'pending') {
      this.loadPendingToilets();
    } else if (activeTab === 'userManage') {
      this.loadUsers();
    } else if (activeTab === 'deletedUsers') {
      this.loadDeletedUsers();
    } else if (activeTab === 'deletedToilets') {
      this.loadDeletedToilets();
    }
  },

  loadPendingToilets: function () {
    var that = this;
    app.request({
      url: '/api/toilet/list',
      method: 'GET',
      success: function (res) {
        if (res.data && res.data.code === 200) {
          var allToilets = res.data.data || [];
          var pending = allToilets.filter(function (t) {
            return t.status === 'pending';
          });
          that.setData({ pendingToilets: pending });
        }
      }
    });
  },

  loadDeletedUsers: function () {
    var that = this;
    app.request({
      url: '/api/user/deleted',
      method: 'GET',
      success: function (res) {
        if (res.data && res.data.code === 200) {
          that.setData({ deletedUsers: res.data.data || [] });
        }
      }
    });
  },

  loadDeletedToilets: function () {
    var that = this;
    app.request({
      url: '/api/toilet/deleted',
      method: 'GET',
      success: function (res) {
        if (res.data && res.data.code === 200) {
          that.setData({ deletedToilets: res.data.data || [] });
        }
      }
    });
  },

  approveToilet: function (e) {
    var that = this;
    var id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认',
      content: '确定审核通过？',
      success: function (res) {
        if (res.confirm) {
          app.showLoading('操作中...');
          app.request({
            url: '/api/toilet/' + id + '/approve',
            method: 'PUT',
            success: function (res2) {
              app.hideLoading();
              if (res2.data && res2.data.code === 200) {
                app.showSuccess('已通过');
                that.loadPendingToilets();
              } else {
                console.error('审核失败:', res2.data);
                app.showError('操作失败');
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

  rejectToilet: function (e) {
    var that = this;
    var id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认',
      content: '确定拒绝？',
      success: function (res) {
        if (res.confirm) {
          app.showLoading('操作中...');
          app.request({
            url: '/api/toilet/' + id + '/reject',
            method: 'PUT',
            success: function (res2) {
              app.hideLoading();
              if (res2.data && res2.data.code === 200) {
                app.showSuccess('已拒绝');
                that.loadPendingToilets();
              } else {
                console.error('拒绝失败:', res2.data);
                app.showError('操作失败');
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

  restoreUser: function (e) {
    var that = this;
    var userId = e.currentTarget.dataset.userid;
    app.showLoading('恢复中...');
    app.request({
      url: '/api/user/' + userId + '/restore',
      method: 'PUT',
      success: function (res) {
        app.hideLoading();
        if (res.data && res.data.code === 200) {
          app.showSuccess('已恢复');
          that.loadDeletedUsers();
        } else {
          console.error('恢复失败:', res.data);
          app.showError('操作失败');
        }
      },
      fail: function () {
        app.hideLoading();
        app.showError('网络错误');
      }
    });
  },

  physicalDeleteUser: function (e) {
    var that = this;
    var userId = e.currentTarget.dataset.userid;
    wx.showModal({
      title: '危险操作',
      content: '确定物理删除用户 ' + userId + '？此操作不可恢复！',
      confirmColor: '#e74c3c',
      success: function (res) {
        if (res.confirm) {
          app.showLoading('删除中...');
          app.request({
            url: '/api/user/' + userId + '/physical',
            method: 'DELETE',
            success: function (res2) {
              app.hideLoading();
              if (res2.data && res2.data.code === 200) {
                app.showSuccess('已物理删除');
                that.loadDeletedUsers();
              } else {
                console.error('物理删除失败:', res2.data);
                app.showError('操作失败');
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

  restoreToilet: function (e) {
    var that = this;
    var id = e.currentTarget.dataset.id;
    app.showLoading('恢复中...');
    app.request({
      url: '/api/toilet/' + id + '/restore',
      method: 'PUT',
      success: function (res) {
        app.hideLoading();
        if (res.data && res.data.code === 200) {
          app.showSuccess('已恢复');
          that.loadDeletedToilets();
        } else {
          console.error('恢复失败:', res.data);
          app.showError('操作失败');
        }
      },
      fail: function () {
        app.hideLoading();
        app.showError('网络错误');
      }
    });
  },

  physicalDeleteToilet: function (e) {
    var that = this;
    var id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '危险操作',
      content: '确定物理删除此厕所？此操作不可恢复！',
      confirmColor: '#e74c3c',
      success: function (res) {
        if (res.confirm) {
          app.showLoading('删除中...');
          app.request({
            url: '/api/toilet/' + id + '/physical',
            method: 'DELETE',
            success: function (res2) {
              app.hideLoading();
              if (res2.data && res2.data.code === 200) {
                app.showSuccess('已物理删除');
                that.loadDeletedToilets();
              } else {
                console.error('物理删除失败:', res2.data);
                app.showError('操作失败');
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

  loadUsers: function () {
    var that = this;
    app.request({
      url: '/api/user/list',
      method: 'GET',
      success: function (res) {
        if (res.data && res.data.code === 200) {
          that.setData({ users: res.data.data || [] });
        }
      }
    });
  },

  addAdminFromList: function (e) {
    var that = this;
    var userId = e.currentTarget.dataset.userid;
    wx.showModal({
      title: '确认',
      content: '确定将 ' + userId + ' 设为管理员？',
      success: function (res) {
        if (res.confirm) {
          app.showLoading('操作中...');
          app.request({
            url: '/api/user/' + userId + '/admin',
            method: 'POST',
            success: function (res2) {
              app.hideLoading();
              if (res2.data && res2.data.code === 200) {
                app.showSuccess('已设为管理员');
                that.loadUsers();
              } else {
                console.error('设为管理员失败:', res2.data);
                app.showError('操作失败');
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

  removeAdminFromList: function (e) {
    var that = this;
    var userId = e.currentTarget.dataset.userid;
    wx.showModal({
      title: '确认',
      content: '确定取消 ' + userId + ' 的管理员权限？',
      success: function (res) {
        if (res.confirm) {
          app.showLoading('操作中...');
          app.request({
            url: '/api/user/' + userId + '/admin',
            method: 'DELETE',
            success: function (res2) {
              app.hideLoading();
              if (res2.data && res2.data.code === 200) {
                app.showSuccess('已取消管理员');
                that.loadUsers();
              } else {
                console.error('取消管理员失败:', res2.data);
                app.showError('操作失败');
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

  deleteUserFromList: function (e) {
    var that = this;
    var userId = e.currentTarget.dataset.userid;
    wx.showModal({
      title: '确认删除',
      content: '确定删除用户 ' + userId + '？',
      confirmColor: '#e74c3c',
      success: function (res) {
        if (res.confirm) {
          app.showLoading('删除中...');
          app.request({
            url: '/api/user/' + userId,
            method: 'DELETE',
            success: function (res2) {
              app.hideLoading();
              if (res2.data && res2.data.code === 200) {
                app.showSuccess('已删除用户');
                that.loadUsers();
              } else {
                console.error('删除用户失败:', res2.data);
                app.showError('操作失败');
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
  }
});