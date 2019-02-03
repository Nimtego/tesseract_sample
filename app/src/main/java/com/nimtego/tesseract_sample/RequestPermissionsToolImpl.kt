package com.nimtego.tesseract_sample

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.widget.Toast


class RequestPermissionsToolImpl : RequestPermissionsTool {
    private val CONFIRMATION_DIALOG = "ConfirmationDialog"
    private var activity: FragmentActivity? = null

    override fun requestPermissions(context: FragmentActivity, permissions: Array<String>) {
        val permissionsMap = HashMap<Int, String>()
        this.activity = context


        for (i in permissions.indices) {
            permissionsMap.put(i, permissions[i])
        }

        for (permission in permissionsMap.entries) {
            if (!isPermissionGranted(context, permission.value)) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission.value)) {

                    ConfirmationDialog.newInstance(permission.key, permission.value)
                        .show(activity!!.supportFragmentManager, CONFIRMATION_DIALOG)
                } else {
                    ActivityCompat.requestPermissions(
                        context, permissions,
                        permission.key
                    )
                    return
                }
            }
        }
    }

    override fun isPermissionsGranted(context: Context, permissions: Array<String>): Boolean {

        for (permission in permissions) {
            if (!isPermissionGranted(context, permission)) {
                return false
            }
        }
        return true
    }

    override fun onPermissionDenied() {
        ErrorDialog.newInstance("Permission needs").show(activity!!.supportFragmentManager, CONFIRMATION_DIALOG)
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    class ConfirmationDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

            return AlertDialog.Builder(getActivity())
                .setMessage("Please allow permission")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    activity?.let {
                        ActivityCompat.requestPermissions(
                            it,
                            arrayOf(arguments?.getString(ARG_PERMISSION)),
                            arguments?.getInt(ARG_REQUEST_CODE)!!
                        )
                    }
                }
                .setNegativeButton(
                    android.R.string.cancel
                ) { _, _ ->
                    Toast.makeText(
                        activity,
                        "Not available",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .create()
        }

        companion object {

            private val ARG_PERMISSION = "permission"
            private val ARG_REQUEST_CODE = "request_code"

            fun newInstance(permissionKey: Int, permissionValue: String): ConfirmationDialog {
                val dialog = ConfirmationDialog()
                val bundle = Bundle()
                bundle.putString(ARG_PERMISSION, permissionValue)
                bundle.putInt(ARG_REQUEST_CODE, permissionKey)
                dialog.setArguments(bundle)
                return dialog
            }
        }
    }

    /**
     * Shows an error message dialog.
     */
    class ErrorDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val activity = activity
            return AlertDialog.Builder(activity)
                .setMessage(arguments?.getString(ARG_MESSAGE))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    //nothing
                }
                .create()
        }

        companion object {

            private val ARG_MESSAGE = "message"

            fun newInstance(message: String): ErrorDialog {
                val dialog = ErrorDialog()
                val args = Bundle()
                args.putString(ARG_MESSAGE, message)
                dialog.arguments = args
                return dialog
            }
        }

    }
}