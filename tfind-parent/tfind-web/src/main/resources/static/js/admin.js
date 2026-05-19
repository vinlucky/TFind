function ajaxPost(url, callback) {
    var xhr = new XMLHttpRequest();
    xhr.open('POST', url, true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                var result = JSON.parse(xhr.responseText);
                if (callback) callback(result);
            } else {
                alert('操作失败，请重试');
            }
        }
    };
    xhr.send();
}

function ajaxDelete(url, callback) {
    var xhr = new XMLHttpRequest();
    xhr.open('DELETE', url, true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                var result = JSON.parse(xhr.responseText);
                if (callback) callback(result);
            } else {
                alert('操作失败，请重试');
            }
        }
    };
    xhr.send();
}

function approveToilet(id) {
    if (!confirm('确定审核通过该厕所？')) return;
    ajaxPost('/admin/toilet/' + id + '/approve', function () {
        alert('审核通过成功');
        location.reload();
    });
}

function rejectToilet(id) {
    if (!confirm('确定审核拒绝该厕所？')) return;
    ajaxPost('/admin/toilet/' + id + '/reject', function () {
        alert('审核拒绝成功');
        location.reload();
    });
}

function deleteUser(userId) {
    if (!confirm('确定删除该用户？此操作为软删除。')) return;
    ajaxDelete('/admin/user/' + userId, function () {
        alert('删除成功');
        location.reload();
    });
}

function deleteToilet(id) {
    if (!confirm('确定删除该厕所？此操作为软删除。')) return;
    ajaxDelete('/admin/toilet/' + id, function () {
        alert('删除成功');
        location.reload();
    });
}

function addAdmin(userId) {
    if (!confirm('确定将该用户设为管理员？')) return;
    ajaxPost('/admin/user/' + userId + '/add-admin', function () {
        alert('添加管理员成功');
        location.reload();
    });
}

function removeAdmin(userId) {
    if (!confirm('确定取消该用户的管理员权限？')) return;
    ajaxPost('/admin/user/' + userId + '/remove-admin', function () {
        alert('取消管理员成功');
        location.reload();
    });
}

function restoreUser(userId) {
    if (!confirm('确定恢复该用户？')) return;
    ajaxPost('/admin/user/' + userId + '/restore', function () {
        alert('恢复成功');
        location.reload();
    });
}

function physicalDeleteUser(userId) {
    if (!confirm('确定物理删除该用户？此操作不可恢复！')) return;
    ajaxPost('/admin/user/' + userId + '/physical-delete', function () {
        alert('物理删除成功');
        location.reload();
    });
}

function restoreToilet(id) {
    if (!confirm('确定恢复该厕所？')) return;
    ajaxPost('/admin/toilet/' + id + '/restore', function () {
        alert('恢复成功');
        location.reload();
    });
}

function physicalDeleteToilet(id) {
    if (!confirm('确定物理删除该厕所？此操作不可恢复！')) return;
    ajaxPost('/admin/toilet/' + id + '/physical-delete', function () {
        alert('物理删除成功');
        location.reload();
    });
}

function switchTab(tabName) {
    var tabs = document.querySelectorAll('.tab-content');
    for (var i = 0; i < tabs.length; i++) {
        tabs[i].classList.remove('active');
    }
    var btns = document.querySelectorAll('.tab-btn');
    for (var i = 0; i < btns.length; i++) {
        btns[i].classList.remove('active');
    }
    document.getElementById('tab-' + tabName).classList.add('active');
    var btnIndex = tabName === 'users' ? 0 : 1;
    btns[btnIndex].classList.add('active');
}
