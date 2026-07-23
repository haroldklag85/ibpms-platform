const responseData = {"degraded":false,"content":{"content":[{"unifiedId":"wd_task_4"}],"pageable":{}}};
const isNestedPage = responseData.content && !Array.isArray(responseData.content) && Array.isArray(responseData.content.content);
const actualItems = isNestedPage ? responseData.content.content : (Array.isArray(responseData.content) ? responseData.content : []);
console.log('isNestedPage:', isNestedPage, 'actualItems length:', actualItems.length);
