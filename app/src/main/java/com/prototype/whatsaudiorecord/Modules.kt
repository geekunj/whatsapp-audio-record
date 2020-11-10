package com.prototype.whatsaudiorecord

import com.prototype.whatsaudiorecord.data.Repository
import com.prototype.whatsaudiorecord.data.local.dao.RecordDao
import com.prototype.whatsaudiorecord.ui.main.MainActivityViewModel
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel

val appModule = module{
    single { Repository(get()) }
    single { RecordDao::class.java }
}

val viewmodelModule = module{

    viewModel{ MainActivityViewModel()}
}